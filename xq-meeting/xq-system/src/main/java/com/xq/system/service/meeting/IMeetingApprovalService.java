package com.xq.system.service.meeting;

import com.xq.common.core.domain.AjaxResult;
import com.xq.system.domain.meeting.MeetingApproval;

import java.util.List;


/**
 * 审批记录Service接口
 *
 * @author xq
 * @date 2025-02-26
 */
public interface IMeetingApprovalService
{
    /**
     * 查询审批记录
     *
     * @param logId 审批记录主键
     * @return 审批记录
     */
    public MeetingApproval selectMeetingApprovalLogByLogId(Long logId);

    /**
     * 查询审批记录列表
     *
     * @param meetingApproval 审批记录
     * @return 审批记录集合
     */
    public List<MeetingApproval> selectMeetingApprovalLogList(MeetingApproval meetingApproval);

    /**
     * 新增审批记录
     *
     * @param meetingApproval 审批记录
     * @return 结果
     */
    public int insertMeetingApprovalLog(MeetingApproval meetingApproval);

    /**
     * 修改审批记录
     *
     * @param meetingApproval 审批记录
     * @return 结果
     */
    public AjaxResult updateMeetingApprovalLog(MeetingApproval meetingApproval);

    /**
     * 删除审批记录
     */
    int deleteMeetingApprovalLogByLogId(Long id);


}
