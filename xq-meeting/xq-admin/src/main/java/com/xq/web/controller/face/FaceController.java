package com.xq.web.controller.face;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.face.MatchRequest;
import com.xq.common.constant.Constants;
import com.xq.common.core.domain.AjaxResult;
import com.xq.common.core.domain.entity.SysUser;
import com.xq.common.utils.SecurityUtils;
import com.xq.service.FaceLoginService;
import com.xq.system.domain.meeting.CmsMeeting;
import com.xq.system.service.ISysUserService;
import com.xq.system.service.meeting.ICmsMeetingService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 人脸登录控制器
 * @Author: xq
 * @CreateTime: 2025-03-23  22:11
 * @Version: 1.0
 */
@RestController
@RequestMapping("/face")
public class FaceController {
    @Autowired
    private FaceLoginService faceLoginService;


    @Autowired
    ICmsMeetingService cmsMeetingService;

    @Autowired
    AipFace client;

    @Autowired
    ISysUserService userService;

    @Value("${baidu.face.imageType}")
    private String imageType;
    @Value("${baidu.face.groupId}")
    private String groupId;

    @PostMapping("/faceLogin")
    public AjaxResult faceLogin(@RequestBody String faceImage) {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = faceLoginService.faceLogin(faceImage);
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    @PostMapping("/registerFace")
    public AjaxResult registerFace(@RequestBody String faceImage) {
        try {
            // 1. 获取当前用户信息
            Long userId = SecurityUtils.getUserId();
            String userName = SecurityUtils.getUsername();

            // 2. 先删除用户已有的人脸信息
            HashMap<String, String> options = new HashMap<>();
            options.put("user_info", userName);

            // 删除百度人脸库中的人脸信息
            JSONObject deleteRes = client.deleteUser(
                    "user_admin",     // 人脸库名称
                    userId.toString(),// 用户ID
                    options
            );

            // 2. 处理Base64图片数据
            faceImage = faceImage.substring(faceImage.indexOf(",") + 1);

            // 3. 调用百度AI进行人脸检测
            HashMap<String, String> detectOptions = new HashMap<>();
            detectOptions.put("face_field", "quality,angle");
            detectOptions.put("max_face_num", "1");

            org.json.JSONObject detectRes = client.detect(faceImage, imageType, detectOptions);

            // 4. 检查人脸检测结果
            if (detectRes.getInt("error_code") != 0) {
                return AjaxResult.error("人脸检测失败：" + detectRes.getString("error_msg"));
            }

            org.json.JSONObject result = detectRes.getJSONObject("result");
            if (result.getInt("face_num") == 0) {
                return AjaxResult.error("未检测到人脸，请确保面部清晰可见");
            }

            // 5. 检查人脸质量
            org.json.JSONObject faceInfo = result.getJSONArray("face_list").getJSONObject(0);
            org.json.JSONObject quality = faceInfo.getJSONObject("quality");

            if (quality.getDouble("blur") > 0.7) {
                return AjaxResult.error("图像模糊，请保持摄像头稳定");
            }

            if (quality.getDouble("illumination") < 40) {
                return AjaxResult.error("光线不足，请确保环境光线充足");
            }

            // 6. 调用百度AI进行人脸注册
            HashMap<String, String> registerOptions = new HashMap<>();
            registerOptions.put("user_info", userName);
            //图片质量控制 NONE: 不进行控制
            // LOW:较低的质量要求
            // NORMAL: 一般的质量要求
            // HIGH: 较高的质量要求 默认 NONE
            registerOptions.put("quality_control", "HIGH");
            //活体检测控制
            // NONE: 不进行控制 LOW:较低的活体要求(高通过率 低攻击拒绝率)
            // NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率)
            // HIGH: 较高的活体要求(高攻击拒绝率 低通过率) 默认NONE
            registerOptions.put("liveness_control", "HIGH");

            JSONObject registerRes = client.addUser(
                    faceImage,
                    imageType,
                    groupId,  // 人脸库名称
                    userId.toString(),  // 用户ID作为百度AI的用户标识
                    registerOptions
            );

            if (registerRes.getInt("error_code") != 0) {
                return AjaxResult.error("人脸注册失败：" + registerRes.getString("error_msg"));
            }
            SysUser sysUser = new SysUser();
            sysUser.setUserId(userId);
            sysUser.setFaceImage(faceImage);
            // 7. 保存用户人脸信息到数据库
            userService.updateUserFaceImageById(sysUser);
            return AjaxResult.success("人脸信息录入成功");

        } catch (Exception e) {
            return AjaxResult.error("人脸注册失败：" + e.getMessage());
        }
    }


    @PostMapping("/faceCheck")
    public AjaxResult faceCheck(@RequestBody CmsMeeting request) {
        try {
            // 1. 获取前端传来的人脸图片(Base64)
            String faceImage = request.getFaceImage();
            // 去掉Base64图片的前缀 "data:image/jpeg;base64,"
            faceImage = faceImage.substring(faceImage.indexOf(",") + 1);

            // 2. 获取当前用户的注册人脸图片
            Long userId = SecurityUtils.getUserId(); // 获取当前登录用户ID
            String registeredFace = userService.getUserFaceImage(userId);
            request.setUserId(userId);
            if (registeredFace == null) {
                return AjaxResult.error("未找到用户注册的人脸信息，请先注册人脸");
            }

            // 3. 调用百度AI进行人脸对比
            MatchRequest req1 = new MatchRequest(faceImage, "BASE64");
            MatchRequest req2 = new MatchRequest(registeredFace, "BASE64");
            ArrayList<MatchRequest> requests = new ArrayList<>();
            requests.add(req1);
            requests.add(req2);

            JSONObject res = client.match(requests);

            // 4. 解析对比结果
            if (res.has("error_code") && res.getInt("error_code") == 0) {
                JSONObject result = res.getJSONObject("result");
                double score = result.getDouble("score");

                // 设置匹配阈值（可以根据实际情况调整，一般80分以上可以认为是同一个人）
                if (score >= 80) {
                    return AjaxResult.success(cmsMeetingService.updateCmsAttendance(request));
                    // 5. 进行签到/签退操作
                } else {
                    return AjaxResult.error("人脸识别失败，相似度不够");
                }
            } else {
                return AjaxResult.error("人脸识别失败：" + res.getString("error_msg"));
            }

        } catch (Exception e) {
            return AjaxResult.error("人脸识别处理失败：" + e.getMessage());
        }
    }
}
