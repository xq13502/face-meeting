package com.xq.system.domain.meeting;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xq.common.annotation.Excel;
import com.xq.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 审批记录对象 meeting_approval_log
 *
 * @author xq
 * @date 2025-02-26
 */

@Data
@Setter
@Getter
@ToString
public class MeetingApproval extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审批编号 */
    private Long id;

    /** 会议编号 */
    @Excel(name = "会议编号")
    private Long meetingId;

    /** 会议标题 */
    @Excel(name = "会议标题")
    private String meetingName;

    /** 审批人编号 */
    @Excel(name = "发起人编号")
    private Long userId;

    /** 审批人姓名 */
    @Excel(name = "发起人姓名")
    private String userName;

    /** 审批状态 */
    @Excel(name = "审批状态",readConverterExp = "0=审批中,1=批准,2=拒绝,3=已过期")
    private String status;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 意见 */
    @Excel(name = "意见")
    private String opinion;

    private CmsMeeting cmsMeeting;




}
