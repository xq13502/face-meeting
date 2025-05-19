package com.xq.system.mapper.meeting;

import java.util.List;

import com.xq.common.core.domain.AjaxResult;
import com.xq.system.domain.meeting.CmsMessage;
import javafx.scene.chart.Axis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 消息Mapper接口
 *
 * @author xq
 * @date 2025-03-02
 */

@Mapper
public interface CmsMessageMapper
{
    /**
     * 查询消息
     *
     * @param messageId 消息主键
     * @return 消息
     */
    public CmsMessage selectCmsMessageByMessageId(@Param("messageId") Long messageId);

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
     * 删除消息
     *
     * @param messageId 消息主键
     * @return 结果
     */
    public int deleteCmsMessageByMessageId(@Param("messageId") Long messageId);

    /**
     * 批量删除消息
     *
     * @param messageIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsMessageByMessageIds(Long[] messageIds);


    int addCmsMessageByAllMeetingUser(@Param("meetingId") Long meetingId,  @Param("type") String type);
}
