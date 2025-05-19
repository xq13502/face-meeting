package com.xq.system.service.impl.meeting;

import com.xq.system.domain.meeting.MeetingRoomEquipment;
import com.xq.system.mapper.meeting.MeetingRoomEquipmentMapper;
import com.xq.system.service.meeting.MeetingRoomEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: ysf
 * @CreateTime: 2025-02-28  10:40
 * @Version: 1.0
 */
@Service
public class MeetingRoomEquipmentServiceImpl  implements MeetingRoomEquipmentService  {
    @Autowired
    MeetingRoomEquipmentMapper meetingRoomEquipmentMapper;
 }
