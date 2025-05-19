package com.xq.web.controller.meeting;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.xq.common.utils.poi.ExcelUtil;

import com.xq.system.domain.meeting.MeetingApproval;
import com.xq.system.service.meeting.IMeetingApprovalService;
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
 * 审批记录Controller
 *
 * @author xq
 * @date 2025-02-26
 */
@RestController
@RequestMapping("/meeting/approval")
public class MeetingApprovalController extends BaseController
{
    @Autowired
    private IMeetingApprovalService meetingApprovalLogService;

    /**
     * 查询审批记录列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:approval:list')")
    @GetMapping("/list")
    public TableDataInfo list(MeetingApproval meetingApproval)
    {
        startPage();
        List<MeetingApproval> list = meetingApprovalLogService.selectMeetingApprovalLogList(meetingApproval);
        return getDataTable(list);
    }

    /**
     * 导出审批记录列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:approval:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MeetingApproval meetingApproval)
    {
        List<MeetingApproval> list = meetingApprovalLogService.selectMeetingApprovalLogList(meetingApproval);
        ExcelUtil<MeetingApproval> util = new ExcelUtil<MeetingApproval>(MeetingApproval.class);
        util.exportExcel(response, list, "审批记录数据");
    }

    /**
     * 获取审批记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:approval:query')")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return success(meetingApprovalLogService.selectMeetingApprovalLogByLogId(logId));
    }

    /**
     * 新增审批记录
     */
    @PreAuthorize("@ss.hasPermi('meeting:approval:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MeetingApproval meetingApproval)
    {
        return toAjax(meetingApprovalLogService.insertMeetingApprovalLog(meetingApproval));
    }

    /**
     * 修改审批记录
     */
    @PreAuthorize("@ss.hasPermi('meeting:approval:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MeetingApproval meetingApproval)
    {
        return AjaxResult.success(meetingApprovalLogService.updateMeetingApprovalLog(meetingApproval));
    }

    /**
     * 删除审批记录
     */
    @PreAuthorize("@ss.hasPermi('meeting:approval:remove')")
	@DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(meetingApprovalLogService.deleteMeetingApprovalLogByLogId(id));
    }
}
