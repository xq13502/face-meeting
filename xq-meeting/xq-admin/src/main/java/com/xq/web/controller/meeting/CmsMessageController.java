package com.xq.web.controller.meeting;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.xq.common.utils.poi.ExcelUtil;
import com.xq.system.domain.meeting.CmsMessage;
import com.xq.system.service.meeting.ICmsMessageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xq.common.annotation.Log;
import com.xq.common.core.controller.BaseController;
import com.xq.common.core.domain.AjaxResult;
import com.xq.common.enums.BusinessType;
import com.xq.common.core.page.TableDataInfo;

/**
 * 消息Controller
 *
 * @author xq
 * @date 2025-03-02
 */
@RestController
@RequestMapping("/meeting/message")
public class CmsMessageController extends BaseController
{
    @Autowired
    private ICmsMessageService cmsMessageService;

    /**
     * 查询消息列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsMessage cmsMessage)
    {
        startPage();
        List<CmsMessage> list = cmsMessageService.selectCmsMessageList(cmsMessage);
        return getDataTable(list);
    }

    /**
     * 导出消息列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:message:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsMessage cmsMessage)
    {
        List<CmsMessage> list = cmsMessageService.selectCmsMessageList(cmsMessage);
        ExcelUtil<CmsMessage> util = new ExcelUtil<CmsMessage>(CmsMessage.class);
        util.exportExcel(response, list, "消息数据");
    }

    /**
     * 获取消息详细信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:message:query')")
    @GetMapping(value = "/{messageId}")
    public AjaxResult getInfo(@PathVariable("messageId") Long messageId)
    {
        return success(cmsMessageService.selectCmsMessageByMessageId(messageId));
    }

    /**
     * 新增消息
     */
    @PreAuthorize("@ss.hasPermi('meeting:message:add')")
    @PostMapping
    public AjaxResult add(@RequestBody CmsMessage cmsMessage)
    {
        return toAjax(cmsMessageService.insertCmsMessage(cmsMessage));
    }

    /**
     * 修改消息
     */
    @PreAuthorize("@ss.hasPermi('meeting:message:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody CmsMessage cmsMessage)
    {
        return toAjax(cmsMessageService.updateCmsMessage(cmsMessage));
    }

    /**
     * 删除消息
     */
    @PreAuthorize("@ss.hasPermi('meeting:message:remove')")
	@DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds)
    {
        return toAjax(cmsMessageService.deleteCmsMessageByMessageIds(messageIds));
    }
}
