package com.xq.system.service.meeting;

import com.xq.common.core.domain.AjaxResult;
import com.xq.system.domain.meeting.CmsLeave;

import java.util.List;

/**
 * 请假Service接口
 *
 * @author xq
 * @date 2025-03-02
 */
public interface ICmsLeaveService
{
    /**
     * 查询请假
     *
     * @param id 请假主键
     * @return 请假
     */
    public CmsLeave selectCmsLeaveById(Long id);

    /**
     * 查询请假列表
     *
     * @param cmsLeave 请假
     * @return 请假集合
     */
    public List<CmsLeave> selectCmsLeaveList(CmsLeave cmsLeave);

    /**
     * 新增请假
     *
     * @param cmsLeave 请假
     * @return 结果
     */
    public AjaxResult insertCmsLeave(CmsLeave cmsLeave);

    /**
     * 修改请假
     *
     * @param cmsLeave 请假
     * @return 结果
     */
    public AjaxResult updateCmsLeave(CmsLeave cmsLeave);

    /**
     * 批量删除请假
     *
     * @param ids 需要删除的请假主键集合
     * @return 结果
     */
    public int deleteCmsLeaveByIds(Long[] ids);

    /**
     * 删除请假信息
     *
     * @param id 请假主键
     * @return 结果
     */
    public int deleteCmsLeaveById(Long id);


    /**
     * 根据id查询请假列表
     */
    List<CmsLeave> selectCmsLeaveListById(CmsLeave cmsLeave);
}
