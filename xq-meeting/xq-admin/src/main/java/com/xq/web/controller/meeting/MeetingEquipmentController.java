package com.xq.web.controller.meeting;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.xq.common.core.domain.entity.SysUser;
import com.xq.system.domain.meeting.MeetingEquipment;
import com.xq.system.service.meeting.IMeetingEquipmentService;
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
 * 会议室设备信息Controller
 *
 * @author xq
 * @date 2025-02-27
 */
@RestController
@RequestMapping("/meeting/equipment")
public class MeetingEquipmentController extends BaseController
{
    @Autowired
    private IMeetingEquipmentService meetingEquipmentService;

    /**
     * 查询会议室设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:equipment:list')")
    @GetMapping("/list")
    public TableDataInfo list(MeetingEquipment meetingEquipment)
    {
        startPage();
        List<MeetingEquipment> list = meetingEquipmentService.selectMeetingEquipmentList(meetingEquipment);
        return getDataTable(list);
    }

    /**
     * 导出会议室设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:equipment:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MeetingEquipment meetingEquipment)
    {
        List<MeetingEquipment> list = meetingEquipmentService.selectMeetingEquipmentList(meetingEquipment);
        ExcelUtil<MeetingEquipment> util = new ExcelUtil<MeetingEquipment>(MeetingEquipment.class);
        util.exportExcel(response, list, "会议室设备信息数据");
    }

    /**
     * 获取会议室设备信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:equipment:query')")
    @GetMapping(value = "/{equipmentId}")
    public AjaxResult getInfo(@PathVariable("equipmentId") Long equipmentId)
    {
        return success(meetingEquipmentService.selectMeetingEquipmentByEquipmentId(equipmentId));
    }

    /**
     * 新增会议室设备信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:equipment:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MeetingEquipment meetingEquipment)
    {
        meetingEquipment.setCreateBy(getUsername());
        return toAjax(meetingEquipmentService.insertMeetingEquipment(meetingEquipment));
    }

    /**
     * 修改会议室设备信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:equipment:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MeetingEquipment meetingEquipment)
    {
        return toAjax(meetingEquipmentService.updateMeetingEquipment(meetingEquipment));
    }


    @PreAuthorize("@ss.hasPermi('meeting:equipment:edit')")
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody MeetingEquipment meetingEquipment) {
        meetingEquipment.setUpdateBy(getUsername());
        return toAjax(meetingEquipmentService.updateEquipmentStatus(meetingEquipment));
    }

    /**
     * 删除会议室设备信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:equipment:remove')")
	@DeleteMapping("/{equipmentIds}")
    public AjaxResult remove(@PathVariable Long[] equipmentIds)
    {
        return toAjax(meetingEquipmentService.deleteMeetingEquipmentByEquipmentIds(equipmentIds));
    }
}
