package com.xq.web.controller.meeting;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.xq.system.domain.meeting.CmsLeave;
import com.xq.system.service.meeting.ICmsLeaveService;
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

import com.xq.common.utils.poi.ExcelUtil;
import com.xq.common.core.page.TableDataInfo;

/**
 * 请假Controller
 *
 * @author xq
 * @date 2025-03-02
 */
@RestController
@RequestMapping("/meeting/leave")
public class CmsLeaveController extends BaseController
{
    @Autowired
    private ICmsLeaveService cmsLeaveService;

    /**
     * 查询请假列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:leave:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsLeave cmsLeave)
    {
        startPage();
        List<CmsLeave> list = cmsLeaveService.selectCmsLeaveList(cmsLeave);
        return getDataTable(list);
    }

    /**
     * 查询请假列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:leave:list')")
    @GetMapping("/getLeaveListById")
    public TableDataInfo getLeaveListById(CmsLeave cmsLeave)
    {
        startPage();
        List<CmsLeave> list = cmsLeaveService.selectCmsLeaveListById(cmsLeave);
        return getDataTable(list);
    }

    /**
     * 导出请假列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:leave:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsLeave cmsLeave)
    {
        List<CmsLeave> list = cmsLeaveService.selectCmsLeaveList(cmsLeave);
        ExcelUtil<CmsLeave> util = new ExcelUtil<CmsLeave>(CmsLeave.class);
        util.exportExcel(response, list, "请假数据");
    }

    /**
     * 获取请假详细信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:leave:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(cmsLeaveService.selectCmsLeaveById(id));
    }

    /**
     * 新增请假
     */
    @PreAuthorize("@ss.hasPermi('meeting:leave:add')")
    @PostMapping
    public AjaxResult add(@RequestBody CmsLeave cmsLeave)
    {
        cmsLeave.setUserId(getUserId());
        cmsLeave.setUserName(getUsername());
        return AjaxResult.success(cmsLeaveService.insertCmsLeave(cmsLeave));
    }

    /**
     * 审批请假
     */
    @PreAuthorize("@ss.hasPermi('meeting:leave:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody CmsLeave cmsLeave)
    {
        return AjaxResult.success(cmsLeaveService.updateCmsLeave(cmsLeave));
    }

    /**
     * 删除请假
     */
    @PreAuthorize("@ss.hasPermi('meeting:leave:remove')")
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(cmsLeaveService.deleteCmsLeaveByIds(ids));
    }
}
