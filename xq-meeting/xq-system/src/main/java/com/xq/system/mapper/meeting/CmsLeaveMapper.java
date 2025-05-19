package com.xq.system.mapper.meeting;

import com.xq.system.domain.meeting.CmsLeave;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 请假Mapper接口
 *
 * @author xq
 * @date 2025-03-02
 */

@Mapper
public interface CmsLeaveMapper
{
    /**
     * 查询请假
     *
     * @param id 请假主键
     * @return 请假
     */
    public CmsLeave selectCmsLeaveById(@Param("id") Long id);

    /**
     * 查询请假列表
     *
     * @param cmsLeave 请假
     * @return 请假集合
     */
    public List<CmsLeave> selectCmsLeaveList(CmsLeave cmsLeave);

    /**
     * 根据当前用户id查询发起会议中所有参会者的请假记录
     *
     * @param userId 请假
     * @return 请假集合
     */
    public List<CmsLeave> getLeaveRecordsByUserId(@Param("userId") Long userId);



    /**
     * 新增请假
     *
     * @param cmsLeave 请假
     * @return 结果
     */
    public int insertCmsLeave(CmsLeave cmsLeave);

    /**
     * 修改请假
     *
     * @param cmsLeave 请假
     * @return 结果
     */
    public int updateCmsLeave(CmsLeave cmsLeave);

    /**
     * 删除请假
     *
     * @param id 请假主键
     * @return 结果
     */
    public int deleteCmsLeaveById(@Param("id") Long id);

    /**
     * 批量删除请假
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsLeaveByIds(Long[] ids);

    CmsLeave selectCurrentCmsLeave(@Param("userId") Long userId, @Param("userName") String userName, @Param("meetingId") Long meetingId, @Param("meetingName") String meetingName);

    /**
     * 查询请假列表
     *
     * @param userId 请假
     * @return 请假集合
     */
    List<CmsLeave> selectCmsLeaveListById(@Param("userId") Long userId);
}
