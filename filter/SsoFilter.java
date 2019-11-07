/**
 *  * Copyright (C): 恒大集团版权所有 Evergrande Group
 *  * FileName: SsoFilter
 *  * Author:   冯达宁
 *  * Date:     2018-01-27 14:36
 *  * Description: 自定义sso单点登录过滤器
 *  
 */
package com.evergrande.smc.filter;
import com.evergrande.smc.common.config.IdmConfig;
import com.evergrande.smc.common.exception.BusinessException;
import com.evergrande.smc.common.model.ErrorCode;
import com.evergrande.smc.redis.LoginRedisClient;
import com.google.common.base.Throwables;
import lombok.extern.log4j.Log4j2;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 自定义sso单点登录过滤器
 *
 * @author 梁翼杰
 * @since 1.0.0
 */
@Log4j2
@Component
public class SsoFilter extends OncePerRequestFilter {

    @Autowired
    private IdmConfig idmConfig;

    /**
     * 注入登录redis接口
     */
    @Autowired
    private LoginRedisClient loginRedisClient;

    /**
     * 拦截处理
     * @param request 请求对象
     * @param response 响应对象
     * @param filterChain 过滤器链
     * @throws BusinessException 已定义异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws BusinessException {
        log.debug("进入SsoFilter：");

        /** 获取用户信息 */
        AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();
        try {
        if (principal != null) {
            String id = principal.getName();
            String loginName = (String) principal.getAttributes().get("smart-alias");
            String mobile = (String) principal.getAttributes().get("mobile");

            /** 单点登录生成uuid存放到redis，并带给前端 */
            String uuid = UUID.randomUUID().toString();
            loginRedisClient.addByKey(uuid, uuid);
            log.info("SSO Login Id：" + id + ", SSO Login UserName：" + loginName + ", SSO Login mobile：" + mobile + ", SSO Login Uuid：" + uuid);

            request.getSession().invalidate();
            request.getSession(true);
            /* 重定向到前端 */
            log.info("重定向到前端: " + idmConfig.getSmcFrontendServerUrl() + "/sso/#/login?uuid=" + uuid + "&loginName=" + loginName);
            response.sendRedirect(idmConfig.getSmcFrontendServerUrl() + "/sso/#/login?uuid=" + uuid + "&loginName=" + loginName);
            return;
        }
        filterChain.doFilter(request, response);
        } catch (IOException e) {
            log.error("IOException class : SsoFilter");
            log.error(Throwables.getStackTraceAsString(e));
            throw BusinessException.define(ErrorCode.IO_Exception);
        } catch (ServletException e) {
            log.error("ServletException class : SsoFilter");
            log.error(Throwables.getStackTraceAsString(e));
            throw BusinessException.define(ErrorCode.SERVLET_EXCEPTION);
        }
    }
}
