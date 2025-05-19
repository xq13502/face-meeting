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
 * 消息对象 cms_message
 *
 * @author xq
 * @date 2025-03-02
 */

@Data
@Setter
@Getter
@ToString
public class CmsMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息编号 */
    private Long messageId;

    /** 发送者编号 */
    @Excel(name = "发送者编号")
    private Long sendId;

    /** 发送者姓名 */
    @Excel(name = "发送者姓名")
    private String sendName;

    /** 接收者编号 */
    @Excel(name = "接收者编号")
    private Long acceptId;


    /** 接收者姓名 */
    @Excel(name = "发送者姓名")
    private String acceptName;
    /** 发送内容 */
    @Excel(name = "发送内容")
    private String content;

    /** 消息类型 */
    @Excel(name = "消息类型",readConverterExp = "0=系统通知,1=会议通知")
    private String type;

    /** 逻辑删除 */
    private Integer isDel;

    /** 状态 */
    @Excel(name = "状态",readConverterExp = "0=未读,1=已读")
    private Integer isRead;

}
