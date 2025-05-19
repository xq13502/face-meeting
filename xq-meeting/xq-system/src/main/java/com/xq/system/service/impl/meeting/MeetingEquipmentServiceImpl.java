package com.xq.system.service.impl.meeting;

import java.util.List;

import com.xq.common.core.domain.entity.SysRole;
import com.xq.common.utils.DateUtils;
import com.xq.common.utils.spring.SpringUtils;
import com.xq.system.domain.meeting.MeetingEquipment;
import com.xq.system.domain.meeting.MeetingRoom;
import com.xq.system.mapper.meeting.MeetingEquipmentMapper;
import com.xq.system.service.meeting.IMeetingEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import com.xq.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;


/**
 * 会议室设备信息Service业务层处理
 *
 * @author xq
 * @date 2025-02-27
 */
@Service
public class MeetingEquipmentServiceImpl implements IMeetingEquipmentService
{
    @Autowired
    private MeetingEquipmentMapper meetingEquipmentMapper;

    /**
     * 查询会议室设备信息
     *
     * @param equipmentId 会议室设备信息主键
     * @return 会议室设备信息
     */
    @Override
    public MeetingEquipment selectMeetingEquipmentByEquipmentId(Long equipmentId)
    {
        return meetingEquipmentMapper.selectMeetingEquipmentByEquipmentId(equipmentId);
    }

    /**
     * 查询会议室设备信息列表
     *
     * @param meetingEquipment 会议室设备信息
     * @return 会议室设备信息
     */
    @Override
    public List<MeetingEquipment> selectMeetingEquipmentList(MeetingEquipment meetingEquipment)
    {
        return meetingEquipmentMapper.selectMeetingEquipmentList(meetingEquipment);
    }

    /**
     * 新增会议室设备信息
     *
     * @param meetingEquipment 会议室设备信息
     * @return 结果
     */
    @Transactional
    @Override
    public int insertMeetingEquipment(MeetingEquipment meetingEquipment)
    {
        meetingEquipment.setCreateTime(DateUtils.getNowDate());
        meetingEquipment.setPurchaseDate(DateUtils.getNowDate());
        return meetingEquipmentMapper.insertMeetingEquipment(meetingEquipment);
    }

    /**
     * 修改会议室设备信息
     *
     * @param meetingEquipment 会议室设备信息
     * @return 结果
     */
    @Transactional
    @Override
    public int updateMeetingEquipment(MeetingEquipment meetingEquipment)
    {
        meetingEquipment.setUpdateTime(DateUtils.getNowDate());
        return meetingEquipmentMapper.updateMeetingEquipment(meetingEquipment);
    }

    /**
     * 批量删除会议室设备信息
     *
     * @param equipmentIds 需要删除的会议室设备信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteMeetingEquipmentByEquipmentIds(Long[] equipmentIds)
    {
        meetingEquipmentMapper.deleteMeetingRoomByRoomIds(equipmentIds);
        return meetingEquipmentMapper.deleteMeetingEquipmentByEquipmentIds(equipmentIds);
    }

    /**
     * 删除会议室设备信息信息
     *
     * @param equipmentId 会议室设备信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteMeetingEquipmentByEquipmentId(Long equipmentId)
    {
        return meetingEquipmentMapper.deleteMeetingEquipmentByEquipmentId(equipmentId);
    }


    /**
     * 修改设备状态
     */
    @Override
    public int updateEquipmentStatus(MeetingEquipment meetingEquipment) {

        return meetingEquipmentMapper.updateMeetingEquipment(meetingEquipment);
    }

    @Override
    public List<MeetingEquipment> selectEquipmentAll() {
        return SpringUtils.getAopProxy(this).selectMeetingEquipmentList(new MeetingEquipment());
    }




}
