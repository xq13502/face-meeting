package com.xq.web.controller.meeting;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.face.MatchRequest;
import com.xq.common.annotation.Anonymous;
import com.xq.common.core.domain.entity.CmsAttendance;
import com.xq.common.core.domain.entity.SysUser;
import com.xq.common.utils.*;
import com.xq.framework.web.service.CmsMeetingTokenService;
import com.xq.system.domain.meeting.MeetingRoom;
import com.xq.system.service.ISysUserService;
import com.xq.system.service.meeting.ICmsAttendanceService;
import com.xq.system.service.meeting.ICmsMeetingService;
import com.xq.system.service.meeting.IMeetingRoomService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.xq.common.annotation.Log;
import com.xq.common.core.controller.BaseController;
import com.xq.common.core.domain.AjaxResult;
import com.xq.common.enums.BusinessType;
import com.xq.system.domain.meeting.CmsMeeting;
import com.xq.common.utils.poi.ExcelUtil;
import com.xq.common.core.page.TableDataInfo;

/**
 * 会议记录Controller
 *
 * @author xq
 * @date 2025-03-02
 */
@RestController
@RequestMapping("/meeting/info")
public class CmsMeetingController extends BaseController {
    @Autowired
    private ICmsMeetingService cmsMeetingService;

    @Autowired
    IMeetingRoomService iMeetingRoomService;

    @Autowired
    ICmsAttendanceService cmsAttendanceService;



    /**
     * 查询会议记录列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsMeeting cmsMeeting) {
        startPage();
        // 获取当前用户角色列表
        List<String> roles = SecurityUtils.getLoginUser().getUser().getRoles()
                .stream()
                .map(role -> role.getRoleKey())
                .collect(Collectors.toList());

        // 判断是否包含 admin 或 meeting 角色
        boolean isMeeting =  roles.contains("meeting");
        boolean admin = SecurityUtils.getLoginUser().getUser().isAdmin();
        if (isMeeting || admin) {
            List<CmsMeeting> cmsMeetings = cmsMeetingService.selectCmsMeetingListByAdmin(cmsMeeting);
            return getDataTable(cmsMeetings);
        } else {
            List<CmsMeeting> list = cmsMeetingService.selectCmsMeetingList(cmsMeeting, getUserId());
            return getDataTable(list);
        }
    }


    /**
     * 查询会议记录列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:info:list')")
    @GetMapping("/getUserAllMeeting")
    public TableDataInfo getUserAllMeeting(CmsMeeting cmsMeeting) {
        startPage();
        // 获取当前用户角色列表
        List<String> roles = SecurityUtils.getLoginUser().getUser().getRoles()
                .stream()
                .map(role -> role.getRoleKey())
                .collect(Collectors.toList());

        // 判断是否包含 admin 或 meeting 角色
        boolean isMeeting =  roles.contains("meeting");
        boolean admin = SecurityUtils.getLoginUser().getUser().isAdmin();
        if (isMeeting || admin) {
            List<CmsMeeting> cmsMeetings = cmsMeetingService.getUserAllMeetingByAdmin(cmsMeeting);
            return getDataTable(cmsMeetings);
        } else {
            List<CmsMeeting> list = cmsMeetingService.getUserAllMeeting(cmsMeeting, getUserId());
            return getDataTable(list);
        }
    }

    /**
     * 导出我发起的会议记录列表
     */
    @PreAuthorize("@ss.hasPermi('meeting:info:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsMeeting cmsMeeting) {
        List<CmsMeeting> list = cmsMeetingService.selectCmsMeetingList(cmsMeeting, getUserId());
        ExcelUtil<CmsMeeting> util = new ExcelUtil<CmsMeeting>(CmsMeeting.class);
        util.exportExcel(response, list, "会议记录数据");
    }

    @PreAuthorize("@ss.hasPermi('meeting:info:export')")
    @PostMapping("/exportUserPrivate")
    public void exportUserPrivate(HttpServletResponse response, CmsMeeting cmsMeeting) {
        List<CmsMeeting> list = cmsMeetingService.getUserAllMeeting(cmsMeeting, getUserId());
        ExcelUtil<CmsMeeting> util = new ExcelUtil<CmsMeeting>(CmsMeeting.class);
        util.exportExcel(response, list, "会议记录数据");
    }


    /**
     * 获取会议记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('meeting:info:query')")
    @GetMapping(value = {"/", "/{meetingId}"})
    public AjaxResult getInfo(@PathVariable(value = "meetingId", required = false) Long meetingId) {
        AjaxResult ajax = AjaxResult.success();
        if (StringUtils.isNotNull(meetingId)) {
            CmsMeeting cmsMeeting = cmsMeetingService.selectCmsMeetingByMeetingId(meetingId);
            if (cmsMeeting != null) {
                ajax.put(AjaxResult.DATA_TAG, cmsMeeting);
                ajax.put("roomIds", cmsMeeting.getMeetingRooms().stream().map(MeetingRoom::getRoomId).collect(Collectors.toList()));
            }
        }
        List<MeetingRoom> meetingRooms = iMeetingRoomService.selectMeetingRoomList(null);
        ajax.put("meetingRooms", meetingRooms);
        return ajax;
    }


    @PreAuthorize("@ss.hasPermi('meeting:info:query')")
    @GetMapping("/getUserList/{meetingId}")
    public AjaxResult getUserList(@PathVariable(value = "meetingId", required = false) Long meetingId
    ) {
        return AjaxResult.success(cmsMeetingService.selectUserList(meetingId));
    }


    /**
     * 新增会议记录
     */
    @PreAuthorize("@ss.hasPermi('meeting:info:add')")
    @PostMapping
    public AjaxResult add(@RequestBody CmsMeeting cmsMeeting) {

        cmsMeeting.setUserId(getUserId());
        cmsMeeting.setUserName(getUsername());
        return AjaxResult.success(cmsMeetingService.insertCmsMeeting(cmsMeeting));
    }

    /**
     * 修改会议记录
     */
    @PreAuthorize("@ss.hasPermi('meeting:info:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody CmsMeeting cmsMeeting) {
        return toAjax(cmsMeetingService.updateCmsMeeting(cmsMeeting));
    }

    /**
     * 删除会议记录
     */
    @PreAuthorize("@ss.hasPermi('meeting:info:remove')")
    @DeleteMapping("/{meetingIds}")
    public AjaxResult remove(@PathVariable Long[] meetingIds) {
        return toAjax(cmsMeetingService.deleteCmsMeetingByMeetingIds(meetingIds));
    }



    /**
     * 正常签到接口
     */
    @PreAuthorize("@ss.hasPermi('meeting:info:check:in')")
    @PostMapping("/checkMeeting")
    public AjaxResult checkMeeting(@RequestBody CmsMeeting cmsMeeting) {
        cmsMeeting.setUserId(getUserId());
        cmsMeeting.setUserName(getUsername());
        return AjaxResult.success(cmsMeetingService.updateCmsAttendance(cmsMeeting));
    }

    /**
     * 代签接口
     */
    @PreAuthorize("@ss.hasPermi('meeting:info:check:in')")
    @PostMapping("/signatureMeeting")
    public AjaxResult signatureMeeting(@RequestBody CmsMeeting cmsMeeting) {
        return AjaxResult.success(cmsMeetingService.signatureMeeting(cmsMeeting));
    }


    /**
     * 查询当前用户当天以及未来三天所需参与的所有会议
     */
    @GetMapping("/getUserFutureMeeting")
    public AjaxResult getUserFutureMeeting() {
        return AjaxResult.success(cmsMeetingService.selectUserFutureMeetingById(getUserId()));
    }




}
