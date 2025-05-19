package com.xq.system.service.impl.meeting;

import java.util.List;
import java.util.stream.Collectors;

import com.xq.common.core.domain.entity.SysUser;
import com.xq.common.utils.DateUtils;
import com.xq.common.utils.SecurityUtils;
import com.xq.system.domain.meeting.CmsMessage;
import com.xq.system.mapper.meeting.CmsMessageMapper;
import com.xq.system.service.meeting.ICmsMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息Service业务层处理
 *
 * @author xq
 * @date 2025-03-02
 */
@Service
public class CmsMessageServiceImpl implements ICmsMessageService
{
    @Autowired
    private CmsMessageMapper cmsMessageMapper;

    /**
     * 查询消息
     *
     * @param messageId 消息主键
     * @return 消息
     */
    @Override
    public CmsMessage selectCmsMessageByMessageId(Long messageId)
    {
        return cmsMessageMapper.selectCmsMessageByMessageId(messageId);
    }

    /**
     * 查询消息列表
     *
     * @param cmsMessage 消息
     * @return 消息
     */
    @Override
    public List<CmsMessage> selectCmsMessageList(CmsMessage cmsMessage)
    {
        // 获取当前用户角色列表
        List<String> roles = SecurityUtils.getLoginUser().getUser().getRoles()
                .stream()
                .map(role -> role.getRoleKey())
                .collect(Collectors.toList());

        // 判断是否包含 admin 或 meeting 角色
        boolean isMeeting =  roles.contains("meeting");
        boolean admin = SecurityUtils.getLoginUser().getUser().isAdmin();
        if (isMeeting || admin) {
            return cmsMessageMapper.selectCmsMessageList(cmsMessage);
        }
            cmsMessage.setAcceptId(SecurityUtils.getUserId());
            return cmsMessageMapper.selectCmsMessageList(cmsMessage);
    }

    /**
     * 新增消息
     *
     * @param cmsMessage 消息
     * @return 结果
     */
    @Override
    public int insertCmsMessage(CmsMessage cmsMessage)
    {
        cmsMessage.setCreateTime(DateUtils.getNowDate());
        return cmsMessageMapper.insertCmsMessage(cmsMessage);
    }

    /**
     * 修改消息
     *
     * @param cmsMessage 消息
     * @return 结果
     */
    @Override
    public int updateCmsMessage(CmsMessage cmsMessage)
    {
        cmsMessage.setUpdateTime(DateUtils.getNowDate());
        return cmsMessageMapper.updateCmsMessage(cmsMessage);
    }

    /**
     * 批量删除消息
     *
     * @param messageIds 需要删除的消息主键
     * @return 结果
     */
    @Override
    public int deleteCmsMessageByMessageIds(Long[] messageIds)
    {
        return cmsMessageMapper.deleteCmsMessageByMessageIds(messageIds);
    }

    /**
     * 删除消息信息
     *
     * @param messageId 消息主键
     * @return 结果
     */
    @Override
    public int deleteCmsMessageByMessageId(Long messageId)
    {
        return cmsMessageMapper.deleteCmsMessageByMessageId(messageId);
    }
}
