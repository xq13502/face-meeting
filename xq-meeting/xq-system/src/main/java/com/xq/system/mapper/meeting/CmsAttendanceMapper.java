package com.xq.system.mapper.meeting;

import com.xq.common.core.domain.entity.CmsAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 签到考勤Mapper接口
 *
 * @author xq
 * @date 2025-03-02s
 */
@Mapper
public interface CmsAttendanceMapper
{
    /**
     *根据会议编号以及用户编号查询签到考勤记录
     * @param id 签到考勤主键
     * @return 签到考勤
     */
    public CmsAttendance selectCmsAttendanceByMeetingId(@Param("id") Long id, @Param("userId") Long userId);


    /**
     * 查询签到考勤
     */
    public CmsAttendance selectCmsAttendanceById(@Param("id") Long id);


    /**
     * 查询签到考勤列表
     *
     * @param cmsAttendance 签到考勤
     * @return 签到考勤集合
     */
    public List<CmsAttendance> selectCmsAttendanceList(CmsAttendance cmsAttendance);

    /**
     * 新增签到考勤
     *
     * @param cmsAttendance 签到考勤
     * @return 结果
     */
    public int insertCmsAttendance(CmsAttendance cmsAttendance);

    /**
     * 修改签到考勤
     *
     * @param cmsAttendance 签到考勤
     * @return 结果
     */
    public int updateCmsAttendance(CmsAttendance cmsAttendance);

    /**
     * 删除签到考勤
     *
     * @param id 签到考勤主键
     * @return 结果
     */
    public int deleteCmsAttendanceById(@Param("id") Long id);

    /**
     * 批量删除签到考勤
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsAttendanceByIds(Long[] ids);

    /**
     * 根据会议编号以及会议成员编号批量插入考勤记录
     */
    public int batchInsertAttendance(@Param("meetingId") Long meetingId);

    /**
     * 查询请假表是否存在当前用户参与会议的请假记录
     */
    CmsAttendance selectCmsAttendanceByMeetingIdAndUserId(@Param("meetingId") Long meetingId, @Param("userId") Long userId);

    /**
     * 批量删除会议的签到考勤记录
     */
    int deleteCmsAttendanceByMeetingId(@Param("meetingIds") Long meetingIds);
}
