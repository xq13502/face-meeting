package com.xq.system.mapper.meeting;

import java.util.Date;
import java.util.List;

import com.xq.common.core.domain.entity.SysUser;
import com.xq.system.domain.meeting.CmsMeeting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会议记录Mapper接口
 *
 * @author xq
 * @date 2025-03-02
 */
@Mapper
public interface CmsMeetingMapper
{
    /**
     * 查询会议记录
     *
     * @param meetingId 会议记录主键
     * @return 会议记录
     */
    public CmsMeeting selectCmsMeetingByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 查询会议记录列表
     *
     * @param cmsMeeting 会议记录
     * @return 会议记录集合
     */
    public List<CmsMeeting> selectCmsMeetingList(@Param("cmsMeeting") CmsMeeting cmsMeeting, @Param("userId") Long userId);

    /**
     * 新增会议记录
     *
     * @param cmsMeeting 会议记录
     * @return 结果
     */
    public int insertCmsMeeting(CmsMeeting cmsMeeting);

    /**
     * 修改会议记录
     *
     * @param cmsMeeting 会议记录
     * @return 结果
     */
    public int updateCmsMeeting(CmsMeeting cmsMeeting);

    /**
     * 删除会议记录
     *
     * @param meetingId 会议记录主键
     * @return 结果
     */
    public int deleteCmsMeetingByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 批量删除会议记录
     *
     * @param meetingIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsMeetingByMeetingIds(Long[] meetingIds);


    /**
     * 查询当前用户参与的所有会议
     */
    List<CmsMeeting> getUserAllMeeting(@Param("cmsMeeting") CmsMeeting cmsMeeting, @Param("userId") Long userId);


    /**
     * 更新应开始的会议（状态1且时间在[start, end)）
     */
    List<CmsMeeting> selectMeetingsToStart(@Param("now") Date now, @Param("todayStart") Date todayStart, @Param("todayEnd") Date todayEnd);

    /**
     * 更新应结束的会议（状态1或3且时间>=end）
     */
    List<CmsMeeting> selectMeetingsToEnd(@Param("now") Date now, @Param("todayStart") Date todayStart, @Param("todayEnd") Date todayEnd);
    /**
     * 查询当前时间是否还存在会议结束未修改状态的会议
     */
    // 在 CmsMeetingMapper 中添加以下方法
    List<CmsMeeting> selectPendingMeetings(@Param("todayStart") Date todayStart, @Param("todayEnd") Date todayEnd);

    List<CmsMeeting> selectCmsMeetingListByAdmin(CmsMeeting cmsMeeting);

    List<CmsMeeting> getUserAllMeetingByAdmin(CmsMeeting cmsMeeting);

    /**
     * //查询用户未来参与会议
     */
    List<CmsMeeting> selectUserFutureMeetingById(@Param("userId") Long userId);
}
