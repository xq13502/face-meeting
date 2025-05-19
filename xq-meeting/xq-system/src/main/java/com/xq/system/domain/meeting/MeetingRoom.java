package com.xq.system.domain.meeting;

import java.util.Arrays;
import java.util.List;
import com.xq.common.annotation.Excel;
import com.xq.common.annotation.Excels;
import com.xq.common.core.domain.BaseEntity;
import com.xq.common.core.domain.entity.SysDept;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 会议室信息对象 meeting_room
 *
 * @author xq
 * @date 2025-02-28
 */
public class MeetingRoom extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 会议室编号 */
    private Long roomId;

    /** 会议室名称 */
    @Excel(name = "会议室名称")
    private String roomName;

    /** 位置信息 */
    @Excel(name = "位置信息")
    private String location;

    /** 容纳人数 */
    @Excel(name = "容纳人数")
    private Integer capacity;

    /** 所属部门 */
    private Long deptId;

    /** 状态 */
    @Excel(name = "状态",readConverterExp = "0=正常,1=停用")
    private String status;

    public MeetingRoom(Long roomId, String roomName, String location, Integer capacity, Long deptId, String status, SysDept dept, Long[] equipmentIds, Long equipmentId, List<MeetingEquipment> equipments) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.location = location;
        this.capacity = capacity;
        this.deptId = deptId;
        this.status = status;
        this.dept = dept;
        this.equipmentIds = equipmentIds;
        this.equipmentId = equipmentId;
        this.equipments = equipments;

    }

    /** 部门对象 */
    @Excels({
            @Excel(name = "部门名称", targetAttr = "deptName", type = Excel.Type.EXPORT),
            @Excel(name = "部门负责人", targetAttr = "leader", type = Excel.Type.EXPORT)
    })
    private SysDept dept;

    private String deptName; // 部门名称查询条件

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
// 生成 getter/setter

    private Long[] equipmentIds;

    public SysDept getDept() {
        return dept;
    }

    public void setDept(SysDept dept) {
        this.dept = dept;
    }

    public Long[] getEquipmentIds() {
        return equipmentIds;
    }

    public void setEquipmentIds(Long[] equipmentIds) {
        this.equipmentIds = equipmentIds;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public MeetingRoom() {
    }

    @Override
    public String toString() {
        return "MeetingRoom{" +
                "roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", location='" + location + '\'' +
                ", capacity=" + capacity +
                ", deptId=" + deptId +
                ", status='" + status + '\'' +
                ", dept=" + dept +
                ", equipmentIds=" + Arrays.toString(equipmentIds) +
                ", equipmentId=" + equipmentId +
                ", equipments=" + equipments +
                ", meetingRoomEquipmentList=" + meetingRoomEquipmentList +
                '}';
    }



    public List<MeetingEquipment> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<MeetingEquipment> equipments) {
        this.equipments = equipments;
    }

    private Long equipmentId;

    /** 会议室和设备关联信息 */
    private List<MeetingEquipment> equipments;
    private List<MeetingRoomEquipment> meetingRoomEquipmentList;

    public void setRoomId(Long roomId)
    {
        this.roomId = roomId;
    }

    public Long getRoomId()
    {
        return roomId;
    }
    public void setRoomName(String roomName)
    {
        this.roomName = roomName;
    }

    public String getRoomName()
    {
        return roomName;
    }
    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getLocation()
    {
        return location;
    }
    public void setCapacity(Integer capacity)
    {
        this.capacity = capacity;
    }

    public Integer getCapacity()
    {
        return capacity;
    }
    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long getDeptId()
    {
        return deptId;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public List<MeetingRoomEquipment> getMeetingRoomEquipmentList()
    {
        return meetingRoomEquipmentList;
    }

    public void setMeetingRoomEquipmentList(List<MeetingRoomEquipment> meetingRoomEquipmentList)
    {
        this.meetingRoomEquipmentList = meetingRoomEquipmentList;
    }

}
