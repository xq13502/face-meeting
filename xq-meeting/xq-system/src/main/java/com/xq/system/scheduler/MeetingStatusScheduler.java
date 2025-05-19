package com.xq.system.scheduler;

import com.xq.common.utils.DateUtils;
import com.xq.system.domain.meeting.CmsMeeting;
import com.xq.system.mapper.meeting.CmsMeetingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class MeetingStatusScheduler implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MeetingStatusScheduler.class);

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    CmsMeetingMapper cmsMeetingMapper;

    private ScheduledFuture<?> scheduledTask;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * 启动任务（如果未运行）
     */
    public void startIfNotRunning() {
        if (isRunning.compareAndSet(false, true)) {
            log.info("启动会议状态定时检查任务");
            // 立即执行一次，之后每分钟检查一次
            scheduledTask = taskScheduler.scheduleAtFixedRate(
                this::updateMeetingStatus,
                Instant.now(),
                Duration.ofMinutes(1)
            );
        }
    }

    /**
     * 停止任务（根据业务需要可选）
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false) && scheduledTask != null) {
            log.info("停止会议状态定时检查任务");
            scheduledTask.cancel(true);
        }
    }

    /**
     * 状态检查逻辑（移除 @Scheduled 注解）
     */
    @Async
    public void updateMeetingStatus() {
        try {
            Date now = DateUtils.getNowDate();
            // 获取当天的开始时间和结束时间
            Date todayStart = DateUtils.getTodayStart(now);
            Date todayEnd = DateUtils.getTodayEnd(now);

            // 更新应开始的会议
            List<CmsMeeting> toStart = cmsMeetingMapper.selectMeetingsToStart(now,todayStart,todayEnd);
            toStart.forEach(meeting -> {
                // 检查会议是否在当天范围内
                if (meeting.getStartTime().after(todayStart) && meeting.getStartTime().before(todayEnd)) {
                    if ("1".equals(meeting.getStatus()) && now.after(meeting.getStartTime())) {
                        meeting.setStatus("3");
                        meeting.setUpdateTime(DateUtils.getNowDate());
                        cmsMeetingMapper.updateCmsMeeting(meeting);
                    }
                }
            });

            // 2. 更新应结束的会议
            List<CmsMeeting> toEnd = cmsMeetingMapper.selectMeetingsToEnd(now,todayStart,todayEnd);
            toEnd.forEach(meeting -> {
                // 检查会议是否在当天范围内
                if (meeting.getEndTime().after(todayStart) && meeting.getEndTime().before(todayEnd)) {
                    if ("0".equals(meeting.getStatus()) || "1".equals(meeting.getStatus()) || "3".equals(meeting.getStatus())
                            && now.after(meeting.getEndTime())) {
                        meeting.setStatus("4");
                        meeting.setUpdateTime(DateUtils.getNowDate());
                        cmsMeetingMapper.updateCmsMeeting(meeting);
                    }
                }
            });

            boolean hasPendingMeetings = hasPendingMeetings(now);

            // 如果没有需要处理的会议，自动停止任务（可选）
            if (toStart.isEmpty() && toEnd.isEmpty() && !hasPendingMeetings) {
                this.stop();
            }
        } catch (Exception e) {
            log.error("会议状态检查异常", e);
        }
    }

    /**
     * 检查是否还有未处理的会议
     */
    private boolean hasPendingMeetings(Date now) {
        // 查询所有未结束的会议
        Date todayStart = DateUtils.getTodayStart(now);
        Date todayEnd = DateUtils.getTodayEnd(now);
        // 查询所有未结束的会议
        List<CmsMeeting> pendingMeetings = cmsMeetingMapper.selectPendingMeetings(todayStart,todayEnd);
        // 过滤出当天的未处理会议
        List<CmsMeeting> todayPendingMeetings = pendingMeetings.stream()
                .filter(meeting -> meeting.getStartTime().after(todayStart) && meeting.getStartTime().before(todayEnd))
                .collect(Collectors.toList());

        return !todayPendingMeetings.isEmpty();
    }

    /**
     * 在 Spring Boot 启动时触发一次任务
     */
    @Override
    public void run(String... args) {
        log.info("应用启动，触发一次会议状态检查任务");
//        updateMeetingStatus();

        try {
            Date now = DateUtils.getNowDate();
            // 1. 更新应开始的会议
            List<CmsMeeting> toStart = cmsMeetingMapper.selectMeetingsToStart(now,null,null);
            toStart.forEach(meeting -> {
                // 检查会议是否在当天范围内
                    if ("1".equals(meeting.getStatus()) && now.after(meeting.getStartTime())) {
                        meeting.setStatus("3");
                        meeting.setUpdateTime(DateUtils.getNowDate());
                        cmsMeetingMapper.updateCmsMeeting(meeting);
                    }
            });

            // 2. 更新应结束的会议
            List<CmsMeeting> toEnd = cmsMeetingMapper.selectMeetingsToEnd(now,null,null);
            toEnd.forEach(meeting -> {
                // 检查会议是否在当天范围内
                    if ("0".equals(meeting.getStatus()) || "1".equals(meeting.getStatus()) || "3".equals(meeting.getStatus())
                            && now.after(meeting.getEndTime())) {
                        meeting.setStatus("4");
                        meeting.setUpdateTime(DateUtils.getNowDate());
                        cmsMeetingMapper.updateCmsMeeting(meeting);
                    }
            });
        } catch (Exception e) {
            log.error("会议状态检查异常", e);
        }
    }

}
