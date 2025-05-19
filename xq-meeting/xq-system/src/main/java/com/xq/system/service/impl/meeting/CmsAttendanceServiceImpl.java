package com.xq.system.service.impl.meeting;

import java.util.List;
import java.util.stream.Collectors;

import com.xq.common.core.domain.AjaxResult;
import com.xq.common.core.domain.entity.CmsAttendance;
import com.xq.common.utils.DateUtils;
import com.xq.common.utils.SecurityUtils;
import com.xq.system.domain.meeting.CmsLeave;
import com.xq.system.mapper.meeting.CmsAttendanceMapper;
import com.xq.system.mapper.meeting.CmsLeaveMapper;
import com.xq.system.service.meeting.ICmsAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 签到考勤Service业务层处理
 *
 * @author xq
 * @date 2025-03-02
 */
@Service
public class CmsAttendanceServiceImpl implements ICmsAttendanceService {
    @Autowired
    private CmsAttendanceMapper cmsAttendanceMapper;

    @Autowired
    private CmsLeaveMapper cmsLeaveMapper;

    /**
     * 查询签到考勤
     *
     * @param id 签到考勤主键
     * @return 签到考勤
     */
    @Override
    public CmsAttendance selectCmsAttendanceByMeetingId(Long id, Long userId) {
        return cmsAttendanceMapper.selectCmsAttendanceByMeetingId(id, userId);
    }

    /**
     * 根据编号获取签到考勤
     */
    @Override
    public CmsAttendance selectCmsAttendanceById(Long id) {
        return cmsAttendanceMapper.selectCmsAttendanceById(id);
    }


    /**
     * 查询签到考勤列表
     *
     * @param cmsAttendance 签到考勤
     * @return 签到考勤
     */
    @Override
    public List<CmsAttendance> selectCmsAttendanceList(CmsAttendance cmsAttendance) {
        return cmsAttendanceMapper.selectCmsAttendanceList(cmsAttendance);
    }

    /**
     * 新增签到考勤
     *
     * @param cmsAttendance 签到考勤
     * @return 结果
     */
    @Override
    @Transactional
    public int insertCmsAttendance(CmsAttendance cmsAttendance) {
        cmsAttendance.setCreateTime(DateUtils.getNowDate());
        return cmsAttendanceMapper.insertCmsAttendance(cmsAttendance);
    }

    /**
     * 修改签到考勤
     *
     * @param cmsAttendance 签到考勤
     * @return 结果
     */
    @Override
    public AjaxResult updateCmsAttendance(CmsAttendance cmsAttendance) {
        if (cmsAttendance == null) {
            return AjaxResult.error("参数异常~");
        }
        cmsAttendance.setUpdateTime(DateUtils.getNowDate());
        int i = cmsAttendanceMapper.updateCmsAttendance(cmsAttendance);
        if (i < 0) {
            return AjaxResult.error("修改失败~");
        }
        return AjaxResult.success("修改成功~");
    }

    /**
     * 批量删除签到考勤
     *
     * @param ids 需要删除的签到考勤主键
     * @return 结果
     */
    @Override
    public int deleteCmsAttendanceByIds(Long[] ids) {
        return cmsAttendanceMapper.deleteCmsAttendanceByIds(ids);
    }

    /**
     * 删除签到考勤信息
     *
     * @param id 签到考勤主键
     * @return 结果
     */
    @Override
    public int deleteCmsAttendanceById(Long id) {
        return cmsAttendanceMapper.deleteCmsAttendanceById(id);
    }
}
