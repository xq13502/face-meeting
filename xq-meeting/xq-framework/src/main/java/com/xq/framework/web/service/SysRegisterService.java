package com.xq.framework.web.service;

import com.xq.system.domain.SysUserPost;
import com.xq.system.domain.SysUserRole;
import com.xq.system.mapper.SysRoleDeptMapper;
import com.xq.system.mapper.SysUserPostMapper;
import com.xq.system.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xq.common.constant.CacheConstants;
import com.xq.common.constant.Constants;
import com.xq.common.constant.UserConstants;
import com.xq.common.core.domain.entity.SysUser;
import com.xq.common.core.domain.model.RegisterBody;
import com.xq.common.core.redis.RedisCache;
import com.xq.common.exception.user.CaptchaException;
import com.xq.common.exception.user.CaptchaExpireException;
import com.xq.common.utils.MessageUtils;
import com.xq.common.utils.SecurityUtils;
import com.xq.common.utils.StringUtils;
import com.xq.framework.manager.AsyncManager;
import com.xq.framework.manager.factory.AsyncFactory;
import com.xq.system.service.ISysConfigService;
import com.xq.system.service.ISysUserService;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册校验方法
 *
 * @author xq
 */
@Component
public class SysRegisterService {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysUserPostMapper sysUserPostMapper;

    private final static Long DefaultRoleId = 6L;
    private final static Long DefaultPostId = 4L;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody) {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username)) {
            msg = "用户名不能为空";
        } else if (StringUtils.isEmpty(password)) {
            msg = "用户密码不能为空";
        } else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            msg = "账户长度必须在2到20个字符之间";
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = "密码长度必须在5到20个字符之间";
        } else if (!userService.checkUserNameUnique(sysUser)) {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        } else {
            sysUser.setNickName(username);
            sysUser.setPassword(SecurityUtils.encryptPassword(password));
            sysUser.setDeptId(Constants.DEPT_ID);
            boolean regFlag = userService.registerUser(sysUser);

            //新增用户和角色关联信息
            sysUserRoleMapper.insertUserRole(sysUser.getUserId(), Constants.ROLE_ID);
            //新增用户和部门关联信息
            //新增用户和岗位关联信息
            sysUserPostMapper.insertUserPost(sysUser.getUserId(), Constants.POST_ID);

            if (!regFlag) {
                msg = "注册失败,请联系系统管理人员";
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException();
        }
    }
}
