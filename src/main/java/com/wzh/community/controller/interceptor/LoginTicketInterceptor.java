package com.wzh.community.controller.interceptor;

import com.wzh.community.entity.LoginTicket;
import com.wzh.community.entity.User;
import com.wzh.community.service.impl.UserServerImpl;
import com.wzh.community.util.CookieUtil;
import com.wzh.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author wzh
 * @data 2022/8/2 -19:51
 * 拦截器，用于处理用户登陆信息
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserServerImpl userServer;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket != null) {
            // 查询凭证
            LoginTicket loginTicket = userServer.findLoginTicket(ticket);
            // 检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) { // 不等于空，不是登出状态，凭证有效期没过
                // 根据凭证查用户
                User user = userServer.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户，用ThreadLocal防止线程安全
                hostHolder.setUsers(user);
                //构建用户认证结果，存入SecurityContext，以便Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        // principal: 主要信息; credentials: 证书; authorities: 权限;
                        user, user.getPassword(), userServer.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

            }


        }

        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
