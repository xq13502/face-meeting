package com.xq.system.domain.meeting;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xq.common.annotation.Excel;
import com.xq.common.core.domain.BaseEntity;
import com.xq.common.core.domain.entity.SysDept;
import com.xq.common.core.domain.entity.SysUser;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.security.core.userdetails.User;

/**
 * 会议记录对象 cms_meeting
 *
 * @author xq
 * @date 2025-03-02
 */

@Setter
@Getter
@ToString
public class CmsMeeting extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 会议编号 */
    private Long meetingId;

    /** 会议名称 */
    @Excel(name = "会议名称")
    private String meetingName;

    /** 会议描述 */
    @Excel(name = "会议描述")
    private String meetingDesc;

    /** 发起人编号 */
    @Excel(name = "发起人编号")
    private Long userId;

    /** 发起人姓名 */
    @Excel(name = "发起人姓名")
    private String userName;

    /** 会议成员编号 */
    @Excel(name = "会议成员编号")
    private String memberNumber;

    /** 会议类型 */
    @Excel(name = "会议类型",readConverterExp = "0=线上,1=线下")
    private Integer type;

    /** 会议室编号 */
    @Excel(name = "会议室编号")
    private Long roomId;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Long timeFrame;

    /** 会议状态 0审核中 1已通过 2未通过 3已开始 4已结束 */
    @Excel(name = "会议状态",readConverterExp = " 0=审核中,1=已通过,2=未通过,3=已开始,4=已结束")
    private String status;

    /** 逻辑删除 */
    private Integer isDel;

    private Long[] roomIds;

    private List<MeetingRoom> meetingRooms;

    private SysUser user;

    private Long deptId;

    private List<Long> userIds;

    private SysDept dept;

    private Long expireTimer;

    private String actionType;

    private String token;

    private String faceImage;


    private String checkMethod;


}
