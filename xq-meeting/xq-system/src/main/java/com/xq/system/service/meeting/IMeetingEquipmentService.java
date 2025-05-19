package com.xq.system.service.meeting;

import com.xq.common.core.domain.entity.SysRole;
import com.xq.system.domain.meeting.MeetingEquipment;

import java.util.List;


/**
 * 会议室设备信息Service接口
 *
 * @author xq
 * @date 2025-02-27
 */
public interface IMeetingEquipmentService
{
    /**
     * 查询会议室设备信息
     *
     * @param equipmentId 会议室设备信息主键
     * @return 会议室设备信息
     */
    public MeetingEquipment selectMeetingEquipmentByEquipmentId(Long equipmentId);

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
     * 批量删除会议室设备信息
     *
     * @param equipmentIds 需要删除的会议室设备信息主键集合
     * @return 结果
     */
    public int deleteMeetingEquipmentByEquipmentIds(Long[] equipmentIds);

    /**
     * 删除会议室设备信息信息
     *
     * @param equipmentId 会议室设备信息主键
     * @return 结果
     */
    public int deleteMeetingEquipmentByEquipmentId(Long equipmentId);

    /**
     * 修改设备状态
     */
    int updateEquipmentStatus(MeetingEquipment meetingEquipment);

    List<MeetingEquipment> selectEquipmentAll();
}
