package com.xq.system.domain.meeting;

import com.xq.common.annotation.Excel;
import com.xq.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 会议室和设备关联对象 meeting_room_equipment
 *
 * @author xq
 * @date 2025-02-28
 */

@Data
@Setter
@Getter
@ToString
public class MeetingRoomEquipment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 会议室编号 */
    @Excel(name = "会议室编号")
    private Long roomId;

    /** 设备编号 */
    @Excel(name = "设备编号")
    private Long equipmentId;

}
