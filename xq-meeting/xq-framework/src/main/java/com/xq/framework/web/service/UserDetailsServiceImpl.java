package com.xq.framework.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.xq.common.core.domain.entity.SysUser;
import com.xq.common.core.domain.model.LoginUser;
import com.xq.common.enums.UserStatus;
import com.xq.common.exception.ServiceException;
import com.xq.common.utils.MessageUtils;
import com.xq.common.utils.StringUtils;
import com.xq.system.service.ISysUserService;

/**
 * 用户验证处理
 *
 * @author xq
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        // 判断是否是人脸登录
        boolean isFaceLogin = username.startsWith("FACE_LOGIN:");
        SysUser user;

        if (isFaceLogin) {
            // 从用户名中提取用户ID
            String userId = username.substring("FACE_LOGIN:".length());
            user = userService.selectUserById(Long.parseLong(userId));
        } else {
            user = userService.selectUserByUserName(username);
        }

        if (StringUtils.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
        else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException(MessageUtils.message("user.password.delete"));
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException(MessageUtils.message("user.blocked"));
        }

        // 只有普通登录才需要验证密码策略
        if (!isFaceLogin) {
            passwordService.validate(user);
        }
        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user)
    {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
