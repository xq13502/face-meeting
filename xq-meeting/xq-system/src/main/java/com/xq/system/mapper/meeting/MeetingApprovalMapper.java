package com.xq.system.mapper.meeting;

import com.xq.system.domain.meeting.MeetingApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 审批记录Mapper接口
 *
 * @author xq
 * @date 2025-02-26
 */

@Mapper
public interface MeetingApprovalMapper
{
    /**
     * 查询审批记录
     *
     * @param id 审批记录主键
     * @return 审批记录
     */
    public MeetingApproval selectMeetingApprovalLogByLogId(@Param("id") Long id);

    /**
     * 查询审批记录
     *
     * @param meetingId 审批记录主键
     * @return 审批记录
     */
    public MeetingApproval selectMeetingApprovalLogByMeetingId(@Param("meetingId") Long meetingId);

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
    public int updateMeetingApprovalLog(MeetingApproval meetingApproval);

    /**
     * 删除审批记录
     *
     * @param id 审批记录主键
     * @return 结果
     */
    public int deleteMeetingApprovalLogByLogId(@Param("id") Long id);

    /**
     * 批量删除审批记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMeetingApprovalLogByLogIds(Long[] ids);


}
