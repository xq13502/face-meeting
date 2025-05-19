package com.xq.system.service.meeting;

import java.util.List;

import com.xq.common.core.domain.AjaxResult;
import com.xq.common.core.domain.entity.SysUser;
import com.xq.system.domain.meeting.CmsMeeting;

/**
 * 会议记录Service接口
 *
 * @author xq
 * @date 2025-03-02
 */
public interface ICmsMeetingService {
    /**
     * 查询会议记录
     *
     * @param meetingId 会议记录主键
     * @return 会议记录
     */
    public CmsMeeting selectCmsMeetingByMeetingId(Long meetingId);

    /**
     * 查询会议记录列表
     *
     * @param cmsMeeting 会议记录
     * @return 会议记录集合
     */
    public List<CmsMeeting> selectCmsMeetingList(CmsMeeting cmsMeeting,Long userId);

    /**
     * 新增会议记录
     *
     * @param cmsMeeting 会议记录
     * @return 结果
     */
    public AjaxResult insertCmsMeeting(CmsMeeting cmsMeeting);

    /**
     * 修改会议记录
     *
     * @param cmsMeeting 会议记录
     * @return 结果
     */
    public int updateCmsMeeting(CmsMeeting cmsMeeting);

    /**
     * 批量删除会议记录
     *
     * @param meetingIds 需要删除的会议记录主键集合
     * @return 结果
     */
    public int deleteCmsMeetingByMeetingIds(Long[] meetingIds);

    /**
     * 删除会议记录信息
     *
     * @param meetingId 会议记录主键
     * @return 结果
     */
    public int deleteCmsMeetingByMeetingId(Long meetingId);


    /**
     * 根据会议编号查询所有用户
     */
    List<SysUser> selectUserList(Long meetingId);

    /**
     *  查询当前用户参与的会议
     */
    List<CmsMeeting> getUserAllMeeting(CmsMeeting cmsMeeting,Long userId);

    /**
     * 确认签到更新考勤签到记录
     */
    AjaxResult updateCmsAttendance(CmsMeeting cmsMeeting);

    /**
     * 手动代签更新考勤签到记录
     */
    AjaxResult signatureMeeting(CmsMeeting cmsMeeting);

    public List<CmsMeeting> selectCmsMeetingListByAdmin(CmsMeeting cmsMeeting);

    public List<CmsMeeting> getUserAllMeetingByAdmin(CmsMeeting cmsMeeting);

    /**
     * //查询用户未来参与会议
     */
    List<CmsMeeting> selectUserFutureMeetingById(Long userId);
}
