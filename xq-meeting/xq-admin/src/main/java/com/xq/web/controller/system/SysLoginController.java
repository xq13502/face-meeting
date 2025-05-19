package com.xq.web.controller.system;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.baidu.aip.face.AipFace;
import com.xq.common.exception.ServiceException;
import com.xq.common.utils.DateUtils;
import com.xq.common.utils.MessageUtils;
import com.xq.common.utils.ip.IpUtils;
import com.xq.framework.manager.AsyncManager;
import com.xq.framework.manager.factory.AsyncFactory;
import com.xq.framework.security.context.AuthenticationContextHolder;
import com.xq.service.FaceLoginService;
import com.xq.system.service.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.xq.common.constant.Constants;
import com.xq.common.core.domain.AjaxResult;
import com.xq.common.core.domain.entity.SysMenu;
import com.xq.common.core.domain.entity.SysUser;
import com.xq.common.core.domain.model.LoginBody;
import com.xq.common.core.domain.model.LoginUser;
import com.xq.common.utils.SecurityUtils;
import com.xq.framework.web.service.SysLoginService;
import com.xq.framework.web.service.SysPermissionService;
import com.xq.framework.web.service.TokenService;

/**
 * 登录验证
 *
 * @author xq
 */
@RestController
@Slf4j
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    ISysRoleService iSysRoleService;


    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {

        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());

        ajax.put(Constants.TOKEN, token);
        return ajax;
    }




    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/getInfo")
    public AjaxResult getInfo()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);

        if (!loginUser.getPermissions().equals(permissions))
        {
            loginUser.setPermissions(permissions);
            tokenService.refreshToken(loginUser);
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("/getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }


}
