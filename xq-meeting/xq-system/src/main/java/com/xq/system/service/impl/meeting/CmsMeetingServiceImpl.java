package com.xq.system.service.impl.meeting;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.xq.common.core.domain.AjaxResult;
import com.xq.common.core.domain.entity.CmsAttendance;
import com.xq.common.core.domain.entity.SysUser;
import com.xq.common.utils.DateUtils;
import com.xq.common.utils.SecurityUtils;
import com.xq.system.domain.meeting.MeetingApproval;
import com.xq.system.mapper.SysUserMapper;
import com.xq.system.mapper.meeting.CmsAttendanceMapper;
import com.xq.system.mapper.meeting.CmsMeetingMapper;
import com.xq.system.mapper.meeting.CmsMessageMapper;
import com.xq.system.mapper.meeting.MeetingApprovalMapper;
import com.xq.system.scheduler.MeetingStatusScheduler;
import com.xq.system.service.meeting.ICmsMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xq.system.domain.meeting.CmsMeeting;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 会议记录Service业务层处理
 *
 * @author xq
 * @date 2025-03-02
 */
@Service
@Slf4j
public class CmsMeetingServiceImpl implements ICmsMeetingService {
    @Autowired
    private CmsMeetingMapper cmsMeetingMapper;

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    CmsAttendanceMapper cmsAttendanceMapper;

    @Autowired
    MeetingApprovalMapper approvalLogMapper;


    @Autowired
    CmsMessageMapper cmsMessageMapper;

    @Autowired
    MeetingStatusScheduler meetingStatusScheduler;

    /**
     * 查询会议记录
     *
     * @param meetingId 会议记录主键
     * @return 会议记录
     */
    @Override
    public CmsMeeting selectCmsMeetingByMeetingId(Long meetingId) {
        return cmsMeetingMapper.selectCmsMeetingByMeetingId(meetingId);
    }

    /**
     * 其他用户查询自己发起的会议记录列表
     *
     * @param cmsMeeting 会议记录
     * @return 会议记录
     */
    @Override
    public List<CmsMeeting> selectCmsMeetingList(CmsMeeting cmsMeeting, Long userId) {
        return cmsMeetingMapper.selectCmsMeetingList(cmsMeeting, userId);
    }

    /**
     * 管理员查询所有会议记录列表
     *
     * @param cmsMeeting
     * @return java.util.List<com.xq.system.domain.meeting.CmsMeeting>
     */

    @Override
    public List<CmsMeeting> selectCmsMeetingListByAdmin(CmsMeeting cmsMeeting) {
        return cmsMeetingMapper.selectCmsMeetingListByAdmin(cmsMeeting);
    }

    /**
     * 管理员可以查询历史所有用户参与的会议
     *
     * @param cmsMeeting
     * @return java.util.List<com.xq.system.domain.meeting.CmsMeeting>
     */
    @Override
    public List<CmsMeeting> getUserAllMeetingByAdmin(CmsMeeting cmsMeeting) {
        return cmsMeetingMapper.getUserAllMeetingByAdmin(cmsMeeting);
    }


    /**
     * @param userId
     * @return com.xq.system.domain.meeting.CmsMeeting
     */
    @Override
    public List<CmsMeeting> selectUserFutureMeetingById(Long userId) {
        return cmsMeetingMapper.selectUserFutureMeetingById(userId);
    }

    /**
     * 新增会议记录
     * <p>
     *
     * @param cmsMeeting 会议记录
     * @return 结果
     */
    @Override
    @Transactional
    public AjaxResult insertCmsMeeting(CmsMeeting cmsMeeting) {
        boolean admin = SecurityUtils.getLoginUser().getUser().isAdmin();
        // 插入会议记录
        cmsMeeting.setCreateTime(DateUtils.getNowDate());
        if (admin || cmsMeeting.getType() == 0) {
            cmsMeeting.setStatus("1");
        }
        int meetingResult = cmsMeetingMapper.insertCmsMeeting(cmsMeeting);

        if (meetingResult < 0) {
            return AjaxResult.error("发起会议失败~");
        }

        //管理员发起会议就触发定时任务修改会议状态
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 动态启动周期性状态检查
                        meetingStatusScheduler.startIfNotRunning();
                    }
                }
        );

        if(admin || cmsMeeting.getType() ==  0){

            int rows = cmsMessageMapper.addCmsMessageByAllMeetingUser(cmsMeeting.getMeetingId(), "1");
            if (rows < 0) {
                return AjaxResult.error("发送会议提醒失败~");
            }

            // 获取插入后的会议ID（需要确保MyBatis配置了主键回填）
            Long meetingId = cmsMeeting.getMeetingId();
            //批量插入考勤记录
            int allAttendanceResult = cmsAttendanceMapper.batchInsertAttendance(meetingId);
            if (allAttendanceResult <= 0) {
                return AjaxResult.error("批量插入考勤记录失败~");
            }
        }

        //线下会议发送审批
        if (!admin && cmsMeeting.getType() == 1) {
            // 插入审批记录
            MeetingApproval approval = new MeetingApproval();
            approval.setMeetingId(cmsMeeting.getMeetingId());
            approval.setMeetingName(cmsMeeting.getMeetingName());
            approval.setUserId(cmsMeeting.getUserId());
            approval.setUserName(cmsMeeting.getUserName());
            approval.setStatus("0"); // 0-审批中
            approval.setCreateTime(DateUtils.getNowDate());

            // 调用审批记录的Mapper
            int allApprovalResult = approvalLogMapper.insertMeetingApprovalLog(approval);
            if (allApprovalResult <= 0) {
                return AjaxResult.error("插入审批记录失败~");

            }
        }

        return AjaxResult.success("发起会议成功~");

    }

    /**
     * 修改会议记录
     *
     * @param cmsMeeting 会议记录
     * @return 结果
     */
    @Override
    public int updateCmsMeeting(CmsMeeting cmsMeeting) {
        cmsMeeting.setUpdateTime(DateUtils.getNowDate());
        return cmsMeetingMapper.updateCmsMeeting(cmsMeeting);
    }

    /**
     * 批量删除会议记录
     *
     * @param meetingIds 需要删除的会议记录主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteCmsMeetingByMeetingIds(Long[] meetingIds) {
        //删除会议记录
        int meetingResult = cmsMeetingMapper.deleteCmsMeetingByMeetingIds(meetingIds);
        if(meetingResult > 0){
           for (Long meetingId : meetingIds){
               cmsAttendanceMapper.deleteCmsAttendanceByMeetingId(meetingId);
           }
        }
        return meetingResult;
        //删除考勤记录
    }

    /**
     * 删除会议记录信息
     *
     * @param meetingId 会议记录主键
     * @return 结果
     */
    @Override
    public int deleteCmsMeetingByMeetingId(Long meetingId) {
        return cmsMeetingMapper.deleteCmsMeetingByMeetingId(meetingId);
    }

    @Override
    public List<SysUser> selectUserList(Long meetingId) {
        return sysUserMapper.selectUsersByIds(meetingId);
    }

    /**
     * 查询当前用户需要参与的所有会议记录
     *
     * @param cmsMeeting
     * @return java.util.List<com.xq.system.domain.meeting.CmsMeeting>
     */
    @Override
    public List<CmsMeeting> getUserAllMeeting(CmsMeeting cmsMeeting, Long userId) {

        return cmsMeetingMapper.getUserAllMeeting(cmsMeeting, userId);
    }


    /**
     * 新增签到记录
     *
     * @param cmsMeeting
     * @return int
     */
    @Override
    public AjaxResult updateCmsAttendance(CmsMeeting cmsMeeting) {
        //查询当前用户是否有签到记录
        return handleAttendance(cmsMeeting);

    }


    public AjaxResult handleAttendance(CmsMeeting cmsMeeting) {

        try {
            CmsMeeting cmsMeetingTime = cmsMeetingMapper.selectCmsMeetingByMeetingId(cmsMeeting.getMeetingId());
            if (cmsMeetingTime == null) {
                return AjaxResult.error("会议不存在~");
            }
            //传入签到/签退方式
            cmsMeetingTime.setCheckMethod(cmsMeeting.getCheckMethod());

            cmsMeetingTime.setActionType(cmsMeeting.getActionType());
            cmsMeetingTime.setUserId(cmsMeeting.getUserId());


            log.info("当前用户编号：{}", cmsMeetingTime.getUserId());
            log.info("当前会议编号：{}", cmsMeetingTime.getMeetingId());
            // 根据会议ID和用户ID查询现有考勤记录
            CmsAttendance existingAttendance = cmsAttendanceMapper.selectCmsAttendanceByMeetingId(
                    cmsMeetingTime.getMeetingId(),
                    cmsMeetingTime.getUserId()
            );
            if (existingAttendance != null) {
                return handleAttendance(existingAttendance, cmsMeetingTime);
            } else {
                return AjaxResult.error("暂未收到会议邀请~");
            }
        } catch (Exception e) {
            return AjaxResult.error("系统错误，请稍后再试");
        }
    }

    private AjaxResult handleAttendance(CmsAttendance existingAttendance, CmsMeeting cmsMeeting) {

        if ("1".equals(existingAttendance.getIsLeave())) {
            return AjaxResult.error("已请假~");
        }
        // 检查会议状态是否为0、2、4
        String meetingStatus = cmsMeeting.getStatus();
        if ("0".equals(meetingStatus)) {
            return AjaxResult.error("会议审批中~");
        }else if("2".equals(meetingStatus)){
            return AjaxResult.error("会议已取消~");
        }else if ("4".equals(meetingStatus)){
            return AjaxResult.error("会议已结束~");
        }

        // 检查会议是否已开始
        Date currentTime = DateUtils.getNowDate();
        if (currentTime.before(cmsMeeting.getStartTime())) {
            return AjaxResult.error("会议未开始~");
        }

        String actionType = cmsMeeting.getActionType();
        String signInStatus = existingAttendance.getSignInStatus();
        String signOutStatus = existingAttendance.getSignOutStatus();
        //当前时间大于开始时间5分钟签到，则视为迟到
        Date startTime = cmsMeeting.getStartTime();
        Instant startInstant = startTime.toInstant();
        boolean isStartExpired = Instant.now().isAfter(startInstant.plus(5, ChronoUnit.MINUTES));
        //当前时间加上5分钟小于结束时间签退，则视为早退
        Date endTime = cmsMeeting.getEndTime();
        Instant now = Instant.now();
        Instant endInstant = endTime.toInstant();
        Instant fiveMinutesBeforeEnd = now.plus(5, ChronoUnit.MINUTES);
        boolean isWithin5MinutesBeforeEnd = fiveMinutesBeforeEnd.isBefore(endInstant);
        existingAttendance.setUpdateTime(DateUtils.getNowDate());

        if ("checkOut".equals(actionType)) {
            if (!"1".equals(signInStatus) & !"2".equals(signInStatus) & !"3".equals(signInStatus)) {
                return AjaxResult.error("请先签到~");
            }
            if ("1".equals(signOutStatus) || "2".equals(signOutStatus) || "3".equals(signOutStatus)) {
                return AjaxResult.error("已签退~");
            }
            if (isWithin5MinutesBeforeEnd) {
                //早退
                existingAttendance.setSignOutStatus("2");
            } else {
                existingAttendance.setSignOutStatus("1");
            }

            if ("face".equals(cmsMeeting.getCheckMethod())) {
                existingAttendance.setSignOutMethod(0);
            } else if ("qrcode".equals(cmsMeeting.getCheckMethod())) {
                existingAttendance.setSignOutMethod(1);
            } else if ("manual".equals(cmsMeeting.getCheckMethod())) {
                existingAttendance.setSignOutMethod(2);
            }
            // 执行签退
            existingAttendance.setId(existingAttendance.getId());
            existingAttendance.setSignOutTime(DateUtils.getNowDate());

            int i = cmsAttendanceMapper.updateCmsAttendance(existingAttendance);
            if (i <= 0) {
                //签退失败
                existingAttendance.setSignOutStatus("4");
                cmsAttendanceMapper.updateCmsAttendance(existingAttendance);
            }
            return AjaxResult.success("签退成功~");

        } else if ("checkIn".equals(actionType)) {
            if ("1".equals(signInStatus) || "2".equals(signInStatus) || "3".equals(signInStatus)) {
                return AjaxResult.error("已签到~");
            }
            if (isStartExpired) {
                //迟到
                existingAttendance.setSignInStatus("2");
            } else {
                existingAttendance.setSignInStatus("1");
            }
            if ("face".equals(cmsMeeting.getCheckMethod())) {
                existingAttendance.setSignInMethod(0);
            } else if ("qrcode".equals(cmsMeeting.getCheckMethod())) {
                existingAttendance.setSignInMethod(1);
            } else if ("manual".equals(cmsMeeting.getCheckMethod())) {
                existingAttendance.setSignInMethod(2);
            }
            // 更新签到信息
            existingAttendance.setId(existingAttendance.getId());
            existingAttendance.setSignInTime(DateUtils.getNowDate());
            int i = cmsAttendanceMapper.updateCmsAttendance(existingAttendance);
            if (i <= 0) {
                //签退失败
                existingAttendance.setSignInStatus("4");
                cmsAttendanceMapper.updateCmsAttendance(existingAttendance);
            }
            return AjaxResult.success("签到成功~");

        } else {
            return AjaxResult.error("无效的操作类型");
        }
    }

    /**
     * 手动代签更新考勤签到记录
     *
     * @param cmsMeeting
     * @return com.xq.common.core.domain.AjaxResult
     */
    @Override
    public AjaxResult signatureMeeting(CmsMeeting cmsMeeting) {

        return handleSignatureAttendance(cmsMeeting);
    }


    public AjaxResult handleSignatureAttendance(CmsMeeting cmsMeeting) {
        try {
            CmsMeeting cmsMeetingTime = cmsMeetingMapper.selectCmsMeetingByMeetingId(cmsMeeting.getMeetingId());
            if (cmsMeetingTime == null) {
                return AjaxResult.error("会议不存在~");
            }
            cmsMeetingTime.setActionType(cmsMeeting.getActionType());
            // 会议为审批和拒绝状态不能代签
            String meetingStatus = cmsMeetingTime.getStatus();
            if ("0".equals(meetingStatus)) {
                return AjaxResult.error("会议审批中~");
            }else if("2".equals(meetingStatus)){
                return AjaxResult.error("会议已取消~");
            }

            // 检查会议是否已开始
            Date currentTime = DateUtils.getNowDate();
            if (currentTime.before(cmsMeetingTime.getStartTime())) {
                return AjaxResult.error("会议未开始~");
            }

            // 获取当前用户角色列表
            List<String> roles = SecurityUtils.getLoginUser().getUser().getRoles()
                    .stream()
                    .map(role -> role.getRoleKey())
                    .collect(Collectors.toList());

            // 判断是否包含 admin 或 meeting 角色
            boolean isMeeting =  roles.contains("meeting");
            boolean admin = SecurityUtils.getLoginUser().getUser().isAdmin();
            Long currentUserId = SecurityUtils.getUserId();
            if (!admin && !isMeeting && !currentUserId.equals(cmsMeetingTime.getUserId())) {
                    return AjaxResult.error("无权限代签~");
            }

            Date endTime = cmsMeetingTime.getEndTime();
            boolean previousOrNextDay = isAfterEndDate(endTime);
            if (previousOrNextDay && !admin) {
                return AjaxResult.error("会议时间已超出代签时间~");
            }

            // 根据会议ID和用户ID查询现有考勤记录
            CmsAttendance existingAttendance = cmsAttendanceMapper.selectCmsAttendanceByMeetingId(
                    cmsMeeting.getMeetingId(),
                    cmsMeeting.getUserId()
            );
            if (existingAttendance != null) {
                return handleSignatureAttendance(existingAttendance, cmsMeetingTime);
            } else {
                return AjaxResult.error("暂未收到会议邀请~");
            }
        } catch (Exception e) {
            return AjaxResult.error("系统错误，请稍后再试");
        }
    }

    private AjaxResult handleSignatureAttendance(CmsAttendance existingAttendance, CmsMeeting cmsMeeting) {
        //代签-手动补签
        existingAttendance.setSignInMethod(2);
        existingAttendance.setSignOutMethod(2);
        existingAttendance.setUpdateTime(DateUtils.getNowDate());

//        if ("1".equals(existingAttendance.getIsLeave())) {
//            return AjaxResult.error("已请假~");
//        }
        String actionType = cmsMeeting.getActionType();
        String signInStatus = existingAttendance.getSignInStatus();
        String signOutStatus = existingAttendance.getSignOutStatus();

        if ("checkOut".equals(actionType)) {
            if (!"1".equals(signInStatus) && !"2".equals(signInStatus) && !"3".equals(signInStatus)) {
                return AjaxResult.error("请先签到~");
            }
            if ("1".equals(signOutStatus) || "2".equals(signOutStatus) || "3".equals(signOutStatus)) {
                return AjaxResult.error("已签退~");
            }
            // 执行代签
            existingAttendance.setId(existingAttendance.getId());
            existingAttendance.setSignOutTime(DateUtils.getNowDate());
            existingAttendance.setSignOutStatus("3");
            cmsAttendanceMapper.updateCmsAttendance(existingAttendance);
            return AjaxResult.success("签退成功~");

        } else if ("checkIn".equals(actionType)) {
            if ("1".equals(signInStatus) || "2".equals(signInStatus) || "3".equals(signInStatus)) {
                return AjaxResult.error("已签到~");
            }
            // 更新签到信息
            existingAttendance.setId(existingAttendance.getId());
            existingAttendance.setSignInTime(DateUtils.getNowDate());
            existingAttendance.setSignInStatus("3");
            cmsAttendanceMapper.updateCmsAttendance(existingAttendance);
            return AjaxResult.success("签到成功~");

        } else {
            return AjaxResult.error("无效的操作类型");
        }
    }

    // 判断当前日期是否在会议结束日期之后
    public static boolean isAfterEndDate(Date endTime) {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 将 Date 转换为 LocalDate
        LocalDate endDate = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // 判断当前日期是否在结束日期之后
        return currentDate.isAfter(endDate);
    }
}


