package com.xq.web.controller.meeting;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.xq.common.core.domain.entity.SysRole;
import com.xq.common.core.domain.entity.SysUser;
import com.xq.common.utils.StringUtils;
import com.xq.system.domain.meeting.MeetingEquipment;
import com.xq.system.domain.meeting.MeetingRoom;
import com.xq.system.service.meeting.IMeetingEquipmentService;
import com.xq.system.service.meeting.IMeetingRoomService;
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
 * 会议室信息Controller
 *
 * @author xq
 * @date 2025-02-28
 */
@RestController
@RequestMapping("/meeting/room")
public class MeetingRoomController extends BaseController {
    @Autowired
    private IMeetingRoomService meetingRoomService;

    @Autowired
    private IMeetingEquipmentService iMeetingEquipmentService;

    /**
     * 查询会议室信息列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:room:list')")
    @GetMapping("/list")
    public TableDataInfo list(MeetingRoom meetingRoom) {
        startPage();
        List<MeetingRoom> list = meetingRoomService.selectMeetingRoomList(meetingRoom);
        return getDataTable(list);
    }

    /**
     * 导出会议室信息列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:room:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MeetingRoom meetingRoom) {
        List<MeetingRoom> list = meetingRoomService.selectMeetingRoomList(meetingRoom);
        ExcelUtil<MeetingRoom> util = new ExcelUtil<MeetingRoom>(MeetingRoom.class);
        util.exportExcel(response, list, "会议室信息数据");
    }

    /**
     * 获取会议室信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:room:query')")
    @GetMapping(value = {"/", "/{roomId}"})
    public AjaxResult getInfo(@PathVariable(value = "roomId", required = false) Long roomId) {

        AjaxResult ajax = AjaxResult.success();

        if (StringUtils.isNotNull(roomId) ) {
            MeetingRoom meetingRoom = meetingRoomService.selectMeetingRoomByRoomId(roomId);
           if(meetingRoom != null){
               ajax.put(AjaxResult.DATA_TAG, meetingRoom);
               ajax.put("equipmentIds", meetingRoom.getEquipments().stream().map(MeetingEquipment::getEquipmentId).collect(Collectors.toList()));
           }
        }
        List<MeetingEquipment> equipments = iMeetingEquipmentService.selectEquipmentAll();
        ajax.put("equipments", equipments);
        return ajax;
    }

    /**
     * 新增会议室信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:room:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MeetingRoom meetingRoom) {
        return toAjax(meetingRoomService.insertMeetingRoom(meetingRoom));
    }

    /**
     * 修改会议室信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:room:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MeetingRoom meetingRoom) {
        meetingRoom.setUpdateBy(getUsername());
        return toAjax(meetingRoomService.updateMeetingRoom(meetingRoom));
    }

    /**
     * 修改会议室信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:room:edit')")
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody MeetingRoom meetingRoom) {
        return toAjax(meetingRoomService.updateMeetingRoomStatus(meetingRoom));
    }

    /**
     * 删除会议室信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:room:remove')")
    @DeleteMapping("/{roomIds}")
    public AjaxResult remove(@PathVariable Long[] roomIds) {
        return toAjax(meetingRoomService.deleteMeetingRoomByRoomIds(roomIds));
    }
}
