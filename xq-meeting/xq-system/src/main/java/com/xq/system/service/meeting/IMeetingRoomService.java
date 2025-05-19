package com.xq.system.service.meeting;

import com.xq.system.domain.meeting.MeetingRoom;

import java.util.List;


/**
 * 会议室信息Service接口
 *
 * @author xq
 * @date 2025-02-28
 */
public interface IMeetingRoomService
{
    /**
     * 查询会议室信息
     *
     * @param roomId 会议室信息主键
     * @return 会议室信息
     */
    public MeetingRoom selectMeetingRoomByRoomId(Long roomId);

    /**
     * 查询会议室信息列表
     *
     * @param meetingRoom 会议室信息
     * @return 会议室信息集合
     */
    public List<MeetingRoom> selectMeetingRoomList(MeetingRoom meetingRoom);

    /**
     * 新增会议室信息
     *
     * @param meetingRoom 会议室信息
     * @return 结果
     */
    public int insertMeetingRoom(MeetingRoom meetingRoom);

    /**
     * 修改会议室信息
     *
     * @param meetingRoom 会议室信息
     * @return 结果
     */
    public int updateMeetingRoom(MeetingRoom meetingRoom);

    /**
     * 批量删除会议室信息
     *
     * @param roomIds 需要删除的会议室信息主键集合
     * @return 结果
     */
    public int deleteMeetingRoomByRoomIds(Long[] roomIds);

    /**
     * 删除会议室信息信息
     *
     * @param roomId 会议室信息主键
     * @return 结果
     */
    public int deleteMeetingRoomByRoomId(Long roomId);

    /**
     * 修改状态
     */

    int updateMeetingRoomStatus(MeetingRoom meetingRoom);
}
