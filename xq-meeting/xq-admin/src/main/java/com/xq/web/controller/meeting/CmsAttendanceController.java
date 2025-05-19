package com.xq.web.controller.meeting;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.xq.common.core.domain.entity.CmsAttendance;
import com.xq.system.service.meeting.ICmsAttendanceService;
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
 * 签到考勤Controller
 *
 * @author xq
 * @date 2025-03-02
 */
@RestController
@RequestMapping("/meeting/attendance")
public class CmsAttendanceController extends BaseController
{
    @Autowired
    private ICmsAttendanceService cmsAttendanceService;

    /**
     * 查询签到考勤列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:attendance:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsAttendance cmsAttendance)
    {
        startPage();
        List<CmsAttendance> list = cmsAttendanceService.selectCmsAttendanceList(cmsAttendance);
        return getDataTable(list);
    }

    /**
     * 导出签到考勤列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:attendance:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsAttendance cmsAttendance)
    {
        List<CmsAttendance> list = cmsAttendanceService.selectCmsAttendanceList(cmsAttendance);
        ExcelUtil<CmsAttendance> util = new ExcelUtil<CmsAttendance>(CmsAttendance.class);
        util.exportExcel(response, list, "签到考勤数据");
    }

    /**
     * 根据会议编号以及用户编号查询签到考勤记录
     */
    @PreAuthorize("@ss.hasPermi('meeting:attendance:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getMeetingInfo(@PathVariable("id") Long id)
    {
        return success(cmsAttendanceService.selectCmsAttendanceByMeetingId(id,getUserId()));
    }
    /**
     * 获取签到考勤详细信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:attendance:query')")
    @GetMapping(value = "/info/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(cmsAttendanceService.selectCmsAttendanceById(id));
    }



    /**
     * 新增签到考勤
     */
    @PreAuthorize("@ss.hasPermi('meeting:attendance:add')")
    @PostMapping
    public AjaxResult add(@RequestBody CmsAttendance cmsAttendance)
    {
        return toAjax(cmsAttendanceService.insertCmsAttendance(cmsAttendance));
    }

    /**
     * 修改签到考勤
     */
    @PreAuthorize("@ss.hasPermi('meeting:attendance:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody CmsAttendance cmsAttendance)
    {
        cmsAttendance.setUserId(getUserId());
        cmsAttendance.setUserName(getUsername());
        return AjaxResult.success(cmsAttendanceService.updateCmsAttendance(cmsAttendance));
    }

    /**
     * 删除签到考勤
     */
    @PreAuthorize("@ss.hasPermi('meeting:attendance:remove')")
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(cmsAttendanceService.deleteCmsAttendanceByIds(ids));
    }
}
