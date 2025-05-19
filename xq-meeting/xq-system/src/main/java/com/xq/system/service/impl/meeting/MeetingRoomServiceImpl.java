package com.xq.system.service.impl.meeting;

import java.util.List;

import com.xq.common.utils.DateUtils;
import com.xq.system.domain.meeting.MeetingRoom;
import com.xq.system.domain.meeting.MeetingRoomEquipment;
import com.xq.system.mapper.meeting.MeetingRoomMapper;
import com.xq.system.service.meeting.IMeetingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import com.xq.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会议室信息Service业务层处理
 *
 * @author xq
 * @date 2025-02-28
 */
@Service
public class MeetingRoomServiceImpl implements IMeetingRoomService {
    @Autowired
    private MeetingRoomMapper meetingRoomMapper;

    /**
     * 查询会议室信息
     *
     * @param roomId 会议室信息主键
     * @return 会议室信息
     */
    @Override
    public MeetingRoom selectMeetingRoomByRoomId(Long roomId) {
        return meetingRoomMapper.selectMeetingRoomByRoomId(roomId);
    }

    /**
     * 查询会议室信息列表
     *
     * @param meetingRoom 会议室信息
     * @return 会议室信息
     */
    @Override
    public List<MeetingRoom> selectMeetingRoomList(MeetingRoom meetingRoom) {
        return meetingRoomMapper.selectMeetingRoomList(meetingRoom);
    }

    /**
     * 新增会议室信息
     *
     * @param meetingRoom 会议室信息
     * @return 结果
     */
    @Transactional
    @Override
    public int insertMeetingRoom(MeetingRoom meetingRoom) {
        meetingRoom.setCreateTime(DateUtils.getNowDate());
        int rows = meetingRoomMapper.insertMeetingRoom(meetingRoom);
        insertMeetingRoomEquipment(meetingRoom);
        return rows;
    }

    /**
     * 修改会议室信息
     *
     * @param meetingRoom 会议室信息
     * @return 结果
     */
    @Transactional
    @Override
    public int updateMeetingRoom(MeetingRoom meetingRoom) {
        meetingRoom.setUpdateTime(DateUtils.getNowDate());
        //删除会议室设备关联信息
        meetingRoomMapper.deleteMeetingRoomEquipmentByRoomId(meetingRoom.getRoomId());
        //添加会议室设备关联信息
        insertMeetingRoomEquipment(meetingRoom);
        return meetingRoomMapper.updateMeetingRoom(meetingRoom);
    }

    public void insertMeetingRoomEquipment(MeetingRoom meetingRoom) {
        insertMeetingRoomEquipment(meetingRoom.getRoomId(), meetingRoom.getEquipmentIds());
    }

    /**
     * 批量删除会议室信息
     *
     * @param roomIds 需要删除的会议室信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteMeetingRoomByRoomIds(Long[] roomIds) {
        meetingRoomMapper.deleteMeetingRoomEquipmentByRoomIds(roomIds);
        return meetingRoomMapper.deleteMeetingRoomByRoomIds(roomIds);
    }

    /**
     * 删除会议室信息信息
     *
     * @param roomId 会议室信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteMeetingRoomByRoomId(Long roomId) {
        meetingRoomMapper.deleteMeetingRoomEquipmentByRoomId(roomId);
        return meetingRoomMapper.deleteMeetingRoomByRoomId(roomId);
    }

    @Override
    public int updateMeetingRoomStatus(MeetingRoom meetingRoom) {
        return meetingRoomMapper.updateMeetingRoom(meetingRoom);
    }

    /**
     * 新增会议室和设备关联信息
     *
     * @param roomId       会议室信息对象
     * @param equipmentIds 会议室信息对象
     */
    public void insertMeetingRoomEquipment(Long roomId, Long[] equipmentIds) {

        if (StringUtils.isNotNull(equipmentIds)) {
            List<MeetingRoomEquipment> list = new ArrayList<MeetingRoomEquipment>(equipmentIds.length);
            for (Long equipmentId : equipmentIds) {
                MeetingRoomEquipment meetingRoomEquipment = new MeetingRoomEquipment();
                meetingRoomEquipment.setRoomId(roomId);
                meetingRoomEquipment.setEquipmentId(equipmentId);
                list.add(meetingRoomEquipment);
            }
            if (list.size() > 0) {
                meetingRoomMapper.batchMeetingRoomEquipment(list);
            }
        }
    }
}
