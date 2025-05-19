package com.xq.system.mapper;

import java.util.List;

import com.xq.common.core.domain.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import com.xq.system.domain.SysUserRole;

/**
 * 用户与角色关联表 数据层
 *
 * @author xq
 */
public interface SysUserRoleMapper
{
    /**
     * 通过用户ID删除用户和角色关联
     *
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteUserRoleByUserId(@Param("userId") Long userId);

    /**
     * 批量删除用户和角色关联
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteUserRole(Long[] ids);

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    public int countUserRoleByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量新增用户角色信息
     *
     * @param userRoleList 用户角色列表
     * @return 结果
     */
    public int batchUserRole(List<SysUserRole> userRoleList);

    /**
     * 删除用户和角色关联信息
     *
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    public int deleteUserRoleInfo(SysUserRole userRole);

    /**
     * 批量取消授权用户角色
     *
     * @param roleId 角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    public int deleteUserRoleInfos(@Param("roleId") Long roleId, @Param("userIds") Long[] userIds);

    /**
     * 根据角色id修改用户部门数据
     */
    public  int updateUserDeptByRoleId(@Param("roleId") Long roleId);

    /**
     * 注册时绑定默认角色
     */
    public int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long defaultRoleId);

    /**
     * 根据用户id查询角色信息
     */
    SysUserRole selectRoleByUserId(@Param("userId") Long userId);
}
