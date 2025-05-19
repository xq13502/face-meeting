package com.xq.system.service.meeting;

import com.xq.system.domain.meeting.CmsMessage;

import java.util.List;

/**
 * 消息Service接口
 *
 * @author xq
 * @date 2025-03-02
 */
public interface ICmsMessageService
{
    /**
     * 查询消息
     *
     * @param messageId 消息主键
     * @return 消息
     */
    public CmsMessage selectCmsMessageByMessageId(Long messageId);

    /**
     * 查询消息列表
     *
     * @param cmsMessage 消息
     * @return 消息集合
     */
    public List<CmsMessage> selectCmsMessageList(CmsMessage cmsMessage);

    /**
     * 新增消息
     *
     * @param cmsMessage 消息
     * @return 结果
     */
    public int insertCmsMessage(CmsMessage cmsMessage);

    /**
     * 修改消息
     *
     * @param cmsMessage 消息
     * @return 结果
     */
    public int updateCmsMessage(CmsMessage cmsMessage);

    /**
     * 批量删除消息
     *
     * @param messageIds 需要删除的消息主键集合
     * @return 结果
     */
    public int deleteCmsMessageByMessageIds(Long[] messageIds);

    /**
     * 删除消息信息
     *
     * @param messageId 消息主键
     * @return 结果
     */
    public int deleteCmsMessageByMessageId(Long messageId);
}
