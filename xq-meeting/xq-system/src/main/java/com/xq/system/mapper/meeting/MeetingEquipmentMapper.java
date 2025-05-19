package com.xq.system.mapper.meeting;

import java.util.List;

import com.xq.system.domain.meeting.MeetingEquipment;
import com.xq.system.domain.meeting.MeetingRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会议室设备信息Mapper接口
 *
 * @author xq
 * @date 2025-02-27
 */

@Mapper
public interface MeetingEquipmentMapper
{
    /**
     * 查询会议室设备信息
     *
     * @param equipmentId 会议室设备信息主键
     * @return 会议室设备信息
     */
    public MeetingEquipment selectMeetingEquipmentByEquipmentId(@Param("equipmentId") Long equipmentId);

    /**
     * 查询会议室设备信息列表
     *
     * @param meetingEquipment 会议室设备信息
     * @return 会议室设备信息集合
     */
    public List<MeetingEquipment> selectMeetingEquipmentList(MeetingEquipment meetingEquipment);

    /**
     * 新增会议室设备信息
     *
     * @param meetingEquipment 会议室设备信息
     * @return 结果
     */
    public int insertMeetingEquipment(MeetingEquipment meetingEquipment);

    /**
     * 修改会议室设备信息
     *
     * @param meetingEquipment 会议室设备信息
     * @return 结果
     */
    public int updateMeetingEquipment(MeetingEquipment meetingEquipment);

    /**
     * 删除会议室设备信息
     *
     * @param equipmentId 会议室设备信息主键
     * @return 结果
     */
    public int deleteMeetingEquipmentByEquipmentId(@Param("equipmentId") Long equipmentId);

    /**
     * 批量删除会议室设备信息
     *
     * @param equipmentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMeetingEquipmentByEquipmentIds(Long[] equipmentIds);

    /**
     * 批量删除会议室信息
     *
     * @param roomId 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMeetingRoomByRoomIds(Long[] roomId);




    /**
     * 修改设备状态
     */
    int updateEquipmentStatus(MeetingEquipment meetingEquipment);
}
