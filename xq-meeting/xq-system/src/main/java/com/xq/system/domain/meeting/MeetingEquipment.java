package com.xq.system.domain.meeting;

import java.util.List;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xq.common.annotation.Excel;
import com.xq.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 会议室设备信息对象 meeting_equipment
 *
 * @author xq
 * @date 2025-02-27
 */
@Data
@Setter
@Getter
@ToString
public class MeetingEquipment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 设备编号 */
    private Long equipmentId;

    /** 设备名称 */
    @Excel(name = "设备名称")
    private String equipmentName;

    /** 设备型号 */
    @Excel(name = "设备型号")
    private String modelNumber;

    /** 设备状态（0正常 1维修中 2已报废） */
    @Excel(name = "设备状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 设备图片 */
    @Excel(name = "设备图片")
    private String imagePath;

    /** 采购日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "采购日期", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date purchaseDate;

    /** 会议室信息信息 */
    private List<MeetingRoom> meetingRoomList;


}
