package com.wzh.community.controller.interceptor;

import com.wzh.community.entity.User;
import com.wzh.community.service.impl.MessageServerImpl;
import com.wzh.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wzh
 * @data 2022/8/20 -21:17
 * 显示私信和通知总的未读消息数量
 * 因为要在任何页面都显示，所以写在拦截器中
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageServerImpl messageServer;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int letterUnreadCount = messageServer.findLetterUnreadCount(user.getId(), null);
            int noticeUnreadCount = messageServer.findNoticeUnreadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);
        }
    }
}
