package com.wzh.community.controller.interceptor;

import com.wzh.community.annotation.LoginRequired;
import com.wzh.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author wzh
 * @data 2022/8/3 -19:41
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    // 参数handler是拦截的目标
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果拦截的是方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod(); // 获取拦截方法
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 如果这个方法有注解并且当前用户没登陆，就不能访问
            if (loginRequired != null && hostHolder.getUser() == null) {
                // 重定向到登陆界面，controller中的return "redirected:xxx"底层用的就是这个方法
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }


        }
        return true;
    }
}
