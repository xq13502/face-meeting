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
 * 请假对象 cms_leave
 *
 * @author xq
 * @date 2025-03-02
 */
@Data
@Setter
@Getter
@ToString
public class CmsLeave extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编号 */
    private Long id;

    /** 请假人 */
    @Excel(name = "请假人")
    private String userName;

    /** 请假人编号 */
    @Excel(name = "请假人编号")
    private Long userId;

    /** 会议名称 */
    @Excel(name = "会议名称")
    private String meetingName;

    /** 会议编号 */
    @Excel(name = "会议编号")
    private Long meetingId;

    /** 请假理由 */
    @Excel(name = "请假理由")
    private String reason;

    /** 请假状态 0审核中 1通过 2拒绝 */
    @Excel(name = "请假状态",readConverterExp = " 0=审核中,1=通过,2=拒绝")
    private String status;

    /** 逻辑删除 */
    private Integer isDel;


}
