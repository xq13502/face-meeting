package com.xq.system.mapper.meeting;

import com.xq.system.domain.meeting.MeetingRoom;
import com.xq.system.domain.meeting.MeetingRoomEquipment;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 会议室信息Mapper接口
 *
 * @author xq
 * @date 2025-02-28
 */
public interface MeetingRoomMapper
{
    /**
     * 查询会议室信息
     *
     * @param roomId 会议室信息主键
     * @return 会议室信息
     */
    public MeetingRoom selectMeetingRoomByRoomId(@Param("roomId") Long roomId);

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
     * 删除会议室信息
     *
     * @param roomId 会议室信息主键
     * @return 结果
     */
    public int deleteMeetingRoomByRoomId(@Param("roomId") Long roomId);

    /**
     * 批量删除会议室信息
     *
     * @param roomIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMeetingRoomByRoomIds(Long[] roomIds);

    /**
     * 批量删除会议室和设备关联
     *
     * @param roomIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMeetingRoomEquipmentByRoomIds(Long[] roomIds);

    /**
     * 批量新增会议室和设备关联
     *
     * @param meetingRoomEquipmentList 会议室和设备关联列表
     * @return 结果
     */
    public int batchMeetingRoomEquipment(List<MeetingRoomEquipment> meetingRoomEquipmentList);


    /**
     * 通过会议室信息主键删除会议室和设备关联信息
     *
     * @param roomId 会议室信息ID
     * @return 结果
     */
    public int deleteMeetingRoomEquipmentByRoomId(@Param("roomId") Long roomId);
}
