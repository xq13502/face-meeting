package com.xq.service;

import com.baidu.aip.face.AipFace;
import com.xq.common.constant.Constants;
import com.xq.common.core.domain.entity.SysUser;
import com.xq.common.core.domain.model.LoginUser;
import com.xq.common.exception.ServiceException;
import com.xq.common.exception.user.UserPasswordNotMatchException;
import com.xq.common.utils.DateUtils;
import com.xq.common.utils.MessageUtils;
import com.xq.common.utils.StringUtils;
import com.xq.common.utils.ip.IpUtils;
import com.xq.framework.manager.AsyncManager;
import com.xq.framework.manager.factory.AsyncFactory;
import com.xq.framework.security.context.AuthenticationContextHolder;
import com.xq.framework.web.service.SysPermissionService;
import com.xq.framework.web.service.TokenService;
import com.xq.system.service.ISysUserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Set;

/**
 * @Description: 人脸登录服务
 * @Author: ysf
 * @CreateTime: 2025-03-20  16:59
 * @Version: 1.0
 */
@Service
public class FaceLoginService {


    @Value("${baidu.face.imageType}")
    private String imageType;
    @Value("${baidu.face.groupId}")
    private String groupId;

    @Autowired
    private AipFace client;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 人脸登录
     */
    public String faceLogin(String faceImage) {
        // 1. 登录前置校验 - 人脸图片基本验证
        loginPreCheck(faceImage);

        // 2. 人脸识别
        JSONObject faceResult = validateFace(faceImage);

        String userId = faceResult.getString("user_id");
        SysUser user = userService.selectUserById(Long.parseLong(userId));
        if (user == null) {
            throw new ServiceException("未找到匹配的用户信息");
        }

        // 3. 构造认证信息（与普通登录保持一致）
        Authentication authentication = null;
        try {
            // 使用特殊前缀标记人脸登录
            String faceLoginUsername = "FACE_LOGIN:" + user.getUserId();
            // 对于人脸登录，使用特殊的密码标记
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(faceLoginUsername, "FACE_LOGIN_PWD");
            AuthenticationContextHolder.setContext(authenticationToken);

            // 进行认证
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getUserName(),
                    Constants.LOGIN_FAIL, e.getMessage()));
            throw new ServiceException(e.getMessage());
        } finally {
            AuthenticationContextHolder.clearContext();
        }

        // 4. 记录登录成功信息
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getUserName(),
                Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));

        // 5. 记录登录信息
        recordLoginInfo(user.getUserId());
        // 6. 生成token（与普通登录一致）
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return tokenService.createToken(loginUser);
    }

    /**
     * 登录前置校验
     */
    private void loginPreCheck(String faceImage) {
        // 图片基本验证
        if (StringUtils.isEmpty(faceImage)) {
            throw new ServiceException("人脸图片数据不能为空");
        }

        // 去除base64前缀
        if (faceImage.contains(",")) {
            faceImage = faceImage.substring(faceImage.indexOf(",") + 1);
        }

        // 验证图片质量
        HashMap<String, String> options = new HashMap<>();
        options.put("face_field", "quality,angle");

        try {
            JSONObject detectRes = client.detect(faceImage, imageType, options);
            if (detectRes.getInt("error_code") != 0) {
                throw new ServiceException("人脸检测失败：" + detectRes.getString("error_msg"));
            }

            JSONObject result = detectRes.getJSONObject("result");
            if (result.getInt("face_num") == 0) {
                throw new ServiceException("未检测到人脸，请确保面部清晰可见");
            }

            // 检查人脸质量
            JSONObject faceInfo = result.getJSONArray("face_list").getJSONObject(0);
            JSONObject quality = faceInfo.getJSONObject("quality");

            if (quality.getDouble("blur") > 0.7) {
                throw new ServiceException("图像模糊，请保持摄像头稳定");
            }

            if (quality.getDouble("illumination") < 40) {
                throw new ServiceException("光线不足，请确保环境光线充足");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("人脸图片校验失败：" + e.getMessage());
        }
    }

    /**
     * 人脸识别验证
     */
    private JSONObject validateFace(String faceImage) {
        if (faceImage.contains(",")) {
            faceImage = faceImage.substring(faceImage.indexOf(",") + 1);
        }
        try {
            HashMap<String, String> options = new HashMap<>();
            //图片质量控制 NONE: 不进行控制
            // LOW:较低的质量要求
            // NORMAL: 一般的质量要求
            // HIGH: 较高的质量要求 默认 NONE
            options.put("quality_control", "HIGH");
            //活体检测控制
            // NONE: 不进行控制 LOW:较低的活体要求(高通过率 低攻击拒绝率)
            // NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率)
            // HIGH: 较高的活体要求(高攻击拒绝率 低通过率) 默认NONE
            options.put("liveness_control", "HIGH");
            options.put("max_user_num", "1");

            JSONObject searchRes = client.search(
                    faceImage,
                    imageType,
                    groupId,
                    options
            );

            if (searchRes.getInt("error_code") != 0) {
                throw new ServiceException("人脸识别失败：" + searchRes.getString("error_msg"));
            }

            JSONObject result = searchRes.getJSONObject("result");
            JSONObject userInfo = result.getJSONArray("user_list").getJSONObject(0);

            // 验证相似度
            if (userInfo.getDouble("score") < 80.0) {
                throw new ServiceException("人脸验证失败，相似度不足");
            }

            return userInfo;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("人脸识别失败：" + e.getMessage());
        }
    }

    /**
     * 记录登录信息
     */
    public void recordLoginInfo(Long userId) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr());
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }
}
