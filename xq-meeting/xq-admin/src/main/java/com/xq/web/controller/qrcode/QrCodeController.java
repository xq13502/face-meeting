package com.xq.web.controller.qrcode;

import com.xq.common.annotation.Anonymous;
import com.xq.common.core.controller.BaseController;
import com.xq.common.core.domain.AjaxResult;
import com.xq.common.core.domain.entity.CmsAttendance;
import com.xq.framework.web.service.CmsMeetingTokenService;
import com.xq.system.domain.meeting.CmsMeeting;
import com.xq.system.service.meeting.ICmsAttendanceService;
import com.xq.system.service.meeting.ICmsMeetingService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 二维码签到控制类
 *
 * @Author: xq
 * @CreateTime: 2025-03-23  22:15
 * @Version: 1.0
 */
@RestController
@RequestMapping("/qrcode")
public class QrCodeController extends BaseController {

    @Autowired
    CmsMeetingTokenService cmsMeetingTokenService;

    @Autowired
    private ICmsMeetingService cmsMeetingService;

    @Autowired
    ICmsAttendanceService cmsAttendanceService;


    /**
     * 生成二维码
     */
    @PostMapping("/generateCheckToken")
    public AjaxResult generateCheckToken(@RequestBody CmsMeeting cmsMeeting) {
        // 构造新 token 信息，有效期 1 分钟
        CmsMeeting tokenInfo = new CmsMeeting();
        tokenInfo.setMeetingId(cmsMeeting.getMeetingId());
        tokenInfo.setUserId(getUserId());
        tokenInfo.setActionType(cmsMeeting.getActionType());
        tokenInfo.setMeetingName(cmsMeeting.getMeetingName());
        tokenInfo.setType(cmsMeeting.getType());
        tokenInfo.setCheckMethod(cmsMeeting.getCheckMethod());
        tokenInfo.setExpireTimer(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));

        // 使用 JWT 生成 token
        String token = cmsMeetingTokenService.createToken(tokenInfo);

        // 生成前端页面 URL（需部署前端页面）
        String url = "http://172.20.10.2:80/sign-page?token=" + token;

        return AjaxResult.success(url);
    }


    /**
     * 效验二维码
     */
    @PostMapping("/verifyCheckToken")
    @Anonymous
    public AjaxResult verifyCheckToken(@RequestBody Map<String, String> params) {
        String token = params.get("token");
        if (token == null) {
            return AjaxResult.error("二维码过期~");
        }
        CmsMeeting cmsMeeting = cmsMeetingTokenService.getCmsMeeting(token);
        if (cmsMeeting == null) {
            return AjaxResult.error("token 验证失败~");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("meetingId", cmsMeeting.getMeetingId());
        result.put("userId", cmsMeeting.getUserId());
        result.put("actionType", cmsMeeting.getActionType());
        result.put("meetingName", cmsMeeting.getMeetingName());

        return AjaxResult.success(result);
    }

    /**
     * 提交签到/签退后插入到考勤记录
     */
    @PostMapping("/confirmSign")
    @Anonymous
    public AjaxResult confirmSign(@RequestBody Map<String, String> params) {
        String token = params.get("token");
        if (token == null || token.isEmpty()) {
            return AjaxResult.error("缺少必要参数~");
        }

        CmsMeeting cmsMeeting = cmsMeetingTokenService.getCmsMeeting(token);

        if (cmsMeeting == null) {
            return AjaxResult.error("token 验证失败~");
        }
        return AjaxResult.success(cmsMeetingService.updateCmsAttendance(cmsMeeting));
    }


    @GetMapping("/signStatus")
    public AjaxResult getSignStatus(CmsMeeting cmsMeeting) {



        CmsAttendance attendance = cmsAttendanceService.selectCmsAttendanceByMeetingId(cmsMeeting.getMeetingId(), getUserId());
        if (attendance == null) {
            return AjaxResult.error("该会议暂未邀请您，请勿重新操作~");
        }

        // 状态优先级：处理中 > 成功/失败 > 未开始
        String status = "pending";


        if ("checkIn".equals(cmsMeeting.getActionType()) && attendance.getSignInTime() == null) {
            return AjaxResult.success().put("status", "pending");
        }

        // 2. 检查签到状态：0=未签到/null=未初始化
        if (ObjectUtils.isEmpty(attendance.getSignInStatus())
                || "0".equals(attendance.getSignInStatus())) {
            return AjaxResult.success().put("status", "need_check_in");
        }
        if ("checkOut".equals(cmsMeeting.getActionType()) && attendance.getSignOutTime() == null) {
            return AjaxResult.success().put("status", "pending");
        }


        // 公共状态判断
        if ("1".equals(attendance.getIsLeave())) {
            return AjaxResult.success().put("status", "leave");
        }

        if ("checkIn".equals(cmsMeeting.getActionType())) {
            if (isOvertime(attendance.getSignInTime())) {
                status = "already_done";
            } else if ("1".equals(attendance.getSignInStatus()) || "2".equals(attendance.getSignInStatus())) {
                status = "success";
            } else if ("4".equals(attendance.getSignInStatus())) {
                status = "error";
            } else if ("0".equals(attendance.getSignInStatus())) { // 添加处理中状态
                status = "processing";
            }
        } else {

            if (isOvertime(attendance.getSignOutTime())) {
                status = "already_done";
            } else if ("1".equals(attendance.getSignOutStatus()) || "2".equals(attendance.getSignOutStatus())) {
                status = "success";
            } else if ("4".equals(attendance.getSignOutStatus())) {
                status = "error";
            } else if ("0".equals(attendance.getSignOutStatus())) { // 添加处理中状态
                status = "processing";
            }
        }

        return AjaxResult.success().put("status", status);
    }

    public boolean isOvertime(Date signTime) {
        // 获取签退时间
        // 转换为Java8时间对象并加10秒
        LocalDateTime adjustedTime = signTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .plusSeconds(10);

        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();

        // 比较时间
        return currentTime.isAfter(adjustedTime);
    }

}
