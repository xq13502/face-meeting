package com.xq.common.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xq.common.annotation.Excel;
import com.xq.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 签到考勤对象 cms_attendance
 *
 * @author xq
 * @date 2025-03-02
 */

@Data
@Setter
@Getter
@ToString
public class CmsAttendance extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编号 */
    private Long id;

    /** 用户编号 */
    @Excel(name = "用户编号")
    private Long userId;

    /** 用户姓名 */
    @Excel(name = "用户姓名")
    private String userName;

    /** 会议编号 */
    @Excel(name = "会议编号")
    private Long meetingId;

    /** 会议名称 */
    @Excel(name = "会议名称")
    private String meetingName;

    /** 是否请假 0否 1请假 */
    @Excel(name = "是否请假",readConverterExp = "0=否,1=是")
    private String isLeave;

    /** 签到状态 0未签到 1已签到 2迟到 */
    @Excel(name = "签到状态",readConverterExp = "0=未签到,1=已签到,2=迟到")
    private String signInStatus;

    /** 签到方式 0 人脸 1扫描  2手动 */
    @Excel(name = "签到方式" ,readConverterExp = "0=人脸,1=扫码,2=手动")
    private Integer signInMethod;

    /** 签到时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "签到时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date signInTime;

    /** 签退状态 0未签退 1已签退 3早退 */
    @Excel(name = "签退状态" ,readConverterExp = "0=未签退,1=已签退,3=早退")
    private String signOutStatus;

    /**  签退方式 0人脸 1扫描  2手动 */
    @Excel(name = "签退方式" ,readConverterExp = "0=人脸,1=扫码,2=手动")
    private Integer signOutMethod;

    /** 签退时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "签退时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date signOutTime;

    /** 逻辑删除 */
    private Integer isDel;





}
