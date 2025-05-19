package com.xq.system.service.impl.meeting;

import java.util.List;
import java.util.stream.Collectors;

import com.xq.common.annotation.DataScope;
import com.xq.common.core.domain.AjaxResult;
import com.xq.common.core.domain.entity.CmsAttendance;
import com.xq.common.core.domain.entity.SysUser;
import com.xq.common.utils.DateUtils;
import com.xq.common.utils.SecurityUtils;
import com.xq.system.domain.meeting.CmsLeave;
import com.xq.system.domain.meeting.CmsMeeting;
import com.xq.system.mapper.meeting.CmsAttendanceMapper;
import com.xq.system.mapper.meeting.CmsLeaveMapper;
import com.xq.system.mapper.meeting.CmsMeetingMapper;
import com.xq.system.mapper.meeting.CmsMessageMapper;
import com.xq.system.service.meeting.ICmsLeaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 请假Service业务层处理
 *
 * @author xq
 * @date 2025-03-02
 */
@Service
@Slf4j
public class CmsLeaveServiceImpl implements ICmsLeaveService {
    @Autowired
    private CmsLeaveMapper cmsLeaveMapper;

    @Autowired
    private CmsAttendanceMapper attendanceMapper;

    @Autowired
    private CmsMeetingMapper cmsMeetingMapper;

    @Autowired
    CmsMessageMapper cmsMessageMapper;


    /**
     * 查询请假
     *
     * @param id 请假主键
     * @return 请假
     */
    @Override
    public CmsLeave selectCmsLeaveById(Long id) {
        return cmsLeaveMapper.selectCmsLeaveById(id);
    }

    /**
     * 查询请假列表
     *
     * @param cmsLeave 请假
     * @return 请假
     */
    @Override
    public List<CmsLeave> selectCmsLeaveList(CmsLeave cmsLeave) {
        // 获取当前用户角色列表
        List<String> roles = SecurityUtils.getLoginUser().getUser().getRoles()
                .stream()
                .map(role -> role.getRoleKey())
                .collect(Collectors.toList());

        // 判断是否包含 admin 或 meeting 角色
        boolean isMeeting = roles.contains("meeting");
        boolean admin = SecurityUtils.getLoginUser().getUser().isAdmin();
        if (isMeeting || admin) {
            // 如果是管理员或会议管理员，查询所有记录
            return cmsLeaveMapper.selectCmsLeaveList(cmsLeave);
        }
        // 否则只查询当前用户的记录
        return cmsLeaveMapper.getLeaveRecordsByUserId(SecurityUtils.getUserId());
    }


    @Override
    public List<CmsLeave> selectCmsLeaveListById(CmsLeave cmsLeave) {
        // 获取当前用户角色列表
        List<String> roles = SecurityUtils.getLoginUser().getUser().getRoles()
                .stream()
                .map(role -> role.getRoleKey())
                .collect(Collectors.toList());

        // 判断是否包含 admin 或 meeting 角色
        boolean isMeeting = roles.contains("meeting");
        boolean admin = SecurityUtils.getLoginUser().getUser().isAdmin();
        if (isMeeting || admin) {
            // 如果是管理员或会议管理员，查询所有记录
            return cmsLeaveMapper.selectCmsLeaveList(cmsLeave);
        }
        // 否则只查询当前用户的记录
        return cmsLeaveMapper.selectCmsLeaveListById(SecurityUtils.getUserId());
    }


    /**
     * 新增请假
     *
     * @param cmsLeave 请假
     * @return 结果
     */
    @Override
    @Transactional
    public AjaxResult insertCmsLeave(CmsLeave cmsLeave) {
        CmsMeeting cmsMeeting = cmsMeetingMapper.selectCmsMeetingByMeetingId(cmsLeave.getMeetingId());
        if ("0".equals(cmsMeeting.getStatus())) {
            return AjaxResult.error("会议审批中~");
        } else if ("2".equals(cmsMeeting.getStatus())) {
            return AjaxResult.error("会议已取消~");
        } else if ("4".equals(cmsMeeting.getStatus())) {
            return AjaxResult.error("会议已结束~");
        }
        CmsAttendance cmsAttendance = attendanceMapper.selectCmsAttendanceByMeetingId(cmsLeave.getMeetingId(), cmsLeave.getUserId());
        if (cmsAttendance == null) {
            return AjaxResult.error("暂未收到会议邀请~");
        }

        if ("1".equals(cmsAttendance.getSignOutStatus()) || "3".equals(cmsAttendance.getSignOutStatus())) {
            return AjaxResult.error("已签退~");
        }

        CmsLeave currentCmsLeave = cmsLeaveMapper.selectCurrentCmsLeave(cmsLeave.getUserId(),
                cmsLeave.getUserName(), cmsLeave.getMeetingId(),
                cmsLeave.getMeetingName());

        if (currentCmsLeave != null) {
            if ("3".equals(currentCmsLeave.getStatus())) {
                return AjaxResult.error("申请已过期~");
            } else if ("1".equals(currentCmsLeave.getStatus())) {
                return AjaxResult.error("申请已通过~");
            } else if ("2".equals(currentCmsLeave.getStatus())) {
                return AjaxResult.error("申请未通过~");
            }
        } else {
            cmsLeave.setCreateTime(DateUtils.getNowDate());
            int row = cmsLeaveMapper.insertCmsLeave(cmsLeave);
            if (row < 0) {
                return AjaxResult.error("申请失败~");
            }
        }
        return AjaxResult.success("申请成功~");
    }

    /**
     * 修改请假
     *
     * @param cmsLeave 请假
     * @return 结果
     */
    @Override
    @Transactional
    public AjaxResult updateCmsLeave(CmsLeave cmsLeave) {

        CmsMeeting cmsMeeting = cmsMeetingMapper.selectCmsMeetingByMeetingId(cmsLeave.getMeetingId());
        if (cmsMeeting == null) {
            return AjaxResult.error("会议不存在~");
        }

        // 判断当前用户是否是管理员或者会议发起人
        Long currentUserId = SecurityUtils.getLoginUser().getUser().getUserId(); // 获取当前登录用户ID
        boolean isMeetingCreator = cmsMeeting.getUserId().equals(currentUserId); // 判断是否是会议发起人
        // 获取当前用户角色列表
        List<String> roles = SecurityUtils.getLoginUser().getUser().getRoles()
                .stream()
                .map(role -> role.getRoleKey())
                .collect(Collectors.toList());

        // 判断是否包含 admin 或 meeting 角色
        boolean isMeeting = roles.contains("meeting");
        boolean admin = SecurityUtils.getLoginUser().getUser().isAdmin();
//         权限判断：只有管理员或会议发起人可以审批
        if (!admin && !isMeeting && !isMeetingCreator) {
            return AjaxResult.error("无权限审批~");
        }


        CmsLeave status = cmsLeaveMapper.selectCmsLeaveById(cmsLeave.getId());
        if ("1".equals(status.getStatus()) || "2".equals(status.getStatus())) {
            return AjaxResult.error("已审批~");
        }

        if ("0".equals(cmsMeeting.getStatus()) || "2".equals(cmsMeeting.getStatus()) || "4".equals(cmsMeeting.getStatus())
                && DateUtils.getNowDate().after(cmsMeeting.getEndTime())) {
            // 已经过期的审批
            cmsLeave.setStatus("3");
            cmsLeaveMapper.updateCmsLeave(cmsLeave);
            cmsMeeting.setStatus("4");
            cmsMeetingMapper.updateCmsMeeting(cmsMeeting);
            return AjaxResult.error("会议已结束：审批失败~");
        }
        cmsLeave.setUpdateTime(DateUtils.getNowDate());
        int row = cmsLeaveMapper.updateCmsLeave(cmsLeave);

        if (row < 0) {
            return AjaxResult.error("审批失败~");
        }

        CmsAttendance cmsAttendance = attendanceMapper.selectCmsAttendanceByMeetingId(cmsLeave.getMeetingId(), cmsLeave.getUserId());
        if (cmsAttendance == null) {
            return AjaxResult.error("考勤记录不存在~");
        }


        if ("1".equals(cmsLeave.getStatus())) { // 同意
            //审批通知
            cmsAttendance.setIsLeave("1"); // 1=已通过
        } else if ("2".equals(cmsLeave.getStatus())) { // 拒绝请假之后还得请假，不签到/签退都算迟到，缺勤
            cmsAttendance.setIsLeave("0"); // 2=未通过
        }
        cmsAttendance.setUpdateTime(DateUtils.getNowDate());
        int currentAttendanceRow = attendanceMapper.updateCmsAttendance(cmsAttendance);
        if (currentAttendanceRow < 0) {
            return AjaxResult.error("审批失败~");
        }

        int i = cmsLeaveMapper.updateCmsLeave(cmsLeave);
        if (i < 0) {
            return AjaxResult.error("审批失败~");
        } else {
            return AjaxResult.success("审批成功~");
        }

    }

    /**
     * 批量删除请假
     *
     * @param ids 需要删除的请假主键
     * @return 结果
     */
    @Override
    public int deleteCmsLeaveByIds(Long[] ids) {
        return cmsLeaveMapper.deleteCmsLeaveByIds(ids);
    }

    /**
     * 删除请假信息
     *
     * @param id 请假主键
     * @return 结果
     */
    @Override
    public int deleteCmsLeaveById(Long id) {
        return cmsLeaveMapper.deleteCmsLeaveById(id);
    }


}
