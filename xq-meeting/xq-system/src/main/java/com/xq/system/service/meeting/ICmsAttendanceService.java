package com.xq.system.service.meeting;

import com.xq.common.core.domain.AjaxResult;
import com.xq.common.core.domain.entity.CmsAttendance;

import java.util.List;

/**
 * 签到考勤Service接口
 *
 * @author xq
 * @date 2025-03-02
 */
public interface ICmsAttendanceService
{
    /**
     * 根据会议编号以及用户编号查询签到考勤记录
     *
     * @param id 签到考勤主键
     * @return 签到考勤
     */
    public CmsAttendance selectCmsAttendanceByMeetingId(Long id,Long userId);


    /**
     * 查询签到考勤
     *
     * @param id 签到考勤主键
     * @return 签到考勤
     */
    public CmsAttendance selectCmsAttendanceById(Long id);

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
    public AjaxResult updateCmsAttendance(CmsAttendance cmsAttendance);

    /**
     * 批量删除签到考勤
     *
     * @param ids 需要删除的签到考勤主键集合
     * @return 结果
     */
    public int deleteCmsAttendanceByIds(Long[] ids);

    /**
     * 删除签到考勤信息
     *
     * @param id 签到考勤主键
     * @return 结果
     */
    public int deleteCmsAttendanceById(Long id);


}
