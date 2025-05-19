package com.xq.system.service.impl.meeting;

import java.util.List;
import java.util.stream.Collectors;

import com.xq.common.core.domain.AjaxResult;
import com.xq.common.utils.DateUtils;
import com.xq.common.utils.SecurityUtils;
import com.xq.system.domain.meeting.CmsMeeting;
import com.xq.system.domain.meeting.MeetingApproval;
import com.xq.system.mapper.meeting.CmsAttendanceMapper;
import com.xq.system.mapper.meeting.CmsMeetingMapper;
import com.xq.system.mapper.meeting.CmsMessageMapper;
import com.xq.system.mapper.meeting.MeetingApprovalMapper;
import com.xq.system.scheduler.MeetingStatusScheduler;
import com.xq.system.service.meeting.IMeetingApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * 审批记录Service业务层处理
 *
 * @author xq
 * @date 2025-02-26
 */
@Service
public class MeetingApprovalServiceImpl implements IMeetingApprovalService {
    @Autowired
    private MeetingApprovalMapper meetingApprovalMapper;

    @Autowired
    CmsMeetingMapper cmsMeetingMapper;

    @Autowired
    CmsMessageMapper cmsMessageMapper;

    @Autowired
    MeetingStatusScheduler meetingStatusScheduler;

    @Autowired
    CmsAttendanceMapper cmsAttendanceMapper;


    /**
     * 查询审批记录
     *
     * @param id 审批记录主键
     * @return 审批记录
     */
    @Override
    public MeetingApproval selectMeetingApprovalLogByLogId(Long id) {
        return meetingApprovalMapper.selectMeetingApprovalLogByLogId(id);
    }

    /**
     * 查询审批记录列表
     *
     * @param meetingApproval 审批记录
     * @return 审批记录
     */
    @Override
    public List<MeetingApproval> selectMeetingApprovalLogList(MeetingApproval meetingApproval) {
        // 获取当前用户角色列表
        List<String> roles = SecurityUtils.getLoginUser().getUser().getRoles()
                .stream()
                .map(role -> role.getRoleKey())
                .collect(Collectors.toList());

        // 判断是否包含 admin 或 meeting 角色
        boolean isMeeting =  roles.contains("meeting");
        boolean admin = SecurityUtils.getLoginUser().getUser().isAdmin();
        if (isMeeting || admin) {
            return meetingApprovalMapper.selectMeetingApprovalLogList(meetingApproval);
        }
        meetingApproval.setUserId(SecurityUtils.getUserId());
        return meetingApprovalMapper.selectMeetingApprovalLogList(meetingApproval);
    }

    /**
     * 新增审批记录
     *
     * @param meetingApproval 审批记录
     * @return 结果
     */
    @Transactional
    @Override
    public int insertMeetingApprovalLog(MeetingApproval meetingApproval) {
        return meetingApprovalMapper.insertMeetingApprovalLog(meetingApproval);
    }

    /**
     * 修改审批记录
     *
     * @param meetingApproval 审批记录
     * @return 结果
     */
    @Transactional
    @Override
    public AjaxResult updateMeetingApprovalLog(MeetingApproval meetingApproval) {

        // 获取当前用户角色列表
        List<String> roles = SecurityUtils.getLoginUser().getUser().getRoles()
                .stream()
                .map(role -> role.getRoleKey())
                .collect(Collectors.toList());

        // 判断是否包含 admin 或 meeting 角色
        boolean isMeeting =  roles.contains("meeting");
        boolean admin = SecurityUtils.getLoginUser().getUser().isAdmin();
        if (!isMeeting && !admin) {
            return AjaxResult.error("无权限审批~");
        }

        try {
            MeetingApproval status = meetingApprovalMapper.selectMeetingApprovalLogByLogId(meetingApproval.getId());
            if ("1".equals(status.getStatus()) || "2".equals(status.getStatus()) || "3".equals(status.getStatus())) {
                return AjaxResult.error("已审批~");
            }

            // 设置审批时间并更新记录
            meetingApproval.setApproveTime(DateUtils.getNowDate());
            int result = meetingApprovalMapper.updateMeetingApprovalLog(meetingApproval);
            if (result <= 0) {
                return AjaxResult.error("审批记录更新失败");
            }

            // 获取会议信息
            CmsMeeting cmsMeeting = cmsMeetingMapper.selectCmsMeetingByMeetingId(meetingApproval.getMeetingId());
            if (cmsMeeting == null) {
                return AjaxResult.error("会议不存在");
            }

            // 检查会议是否已过期（状态为0或4且时间已过结束时间）
            if (("0".equals(cmsMeeting.getStatus()) || "4".equals(cmsMeeting.getStatus()))
                    && DateUtils.getNowDate().after(cmsMeeting.getEndTime())) {
                meetingApproval.setStatus("3");
                meetingApprovalMapper.updateMeetingApprovalLog(meetingApproval);
                cmsMeeting.setStatus("4");
                cmsMeetingMapper.updateCmsMeeting(cmsMeeting);
                return AjaxResult.error("会议已结束：审批失败~");
            }

            boolean flag = false;
            // 根据审批状态更新会议状态
            if ("1".equals(meetingApproval.getStatus())) {
                cmsMeeting.setStatus("1"); // 审批通过
                flag = true;
                // 异步触发动态定时任务（仅在审批通过时注册）
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                // 动态启动周期性状态检查
                                meetingStatusScheduler.startIfNotRunning();
                            }
                        }
                );
            } else if ("2".equals(meetingApproval.getStatus())) {
                cmsMeeting.setStatus("2"); // 审批拒绝
            } else {
                return AjaxResult.error("无效的审批状态~");
            }

            // 提交会议状态更新
            int meetingUpdateResult = cmsMeetingMapper.updateCmsMeeting(cmsMeeting);
            if (meetingUpdateResult <= 0) {
                return AjaxResult.error("会议审批失败~");
            }

            //审批通过发送会议提醒给用户
            if (flag) {
                int addCmsMessageByAllMeetingUser = cmsMessageMapper.addCmsMessageByAllMeetingUser(cmsMeeting.getMeetingId(), "1");
                if (addCmsMessageByAllMeetingUser < 0) {
                    return AjaxResult.error("发送会议提醒失败~");
                }

                // 获取需要插入考勤的会议ID
                Long meetingId = cmsMeeting.getMeetingId();
                //批量插入考勤记录
                int allAttendanceResult = cmsAttendanceMapper.batchInsertAttendance(meetingId);
                if (allAttendanceResult <= 0) {
                    return AjaxResult.error("批量插入考勤记录失败~");
                }

            }

            return AjaxResult.success("1".equals(meetingApproval.getStatus()) ? "已同意~" : "已拒绝~");
        } catch (Exception e) {
            return AjaxResult.error("审批失败：" + e.getMessage());
        }
    }



    /**
     * 删除审批记录信息
     *
     * @param id 审批记录主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteMeetingApprovalLogByLogId(Long id) {
        return meetingApprovalMapper.deleteMeetingApprovalLogByLogId(id);
    }


}
