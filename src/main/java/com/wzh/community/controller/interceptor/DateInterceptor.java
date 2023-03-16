package com.wzh.community.controller.interceptor;

import com.wzh.community.entity.User;
import com.wzh.community.service.impl.DataServerImpl;
import com.wzh.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wzh
 * @data 2022/9/23 -19:08
 */
@Component
public class DateInterceptor implements HandlerInterceptor {
    @Autowired
    private DataServerImpl dataServer;

    @Autowired
    private HostHolder hostHolder;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计UV
        String ip = request.getRemoteHost(); // 获取ip
        dataServer.recordUV(ip);


        // 统计DAU
        User user = hostHolder.getUser();
        if (user != null) {
            dataServer.recordDAU(user.getId());
        }

        return true;
    }
}
