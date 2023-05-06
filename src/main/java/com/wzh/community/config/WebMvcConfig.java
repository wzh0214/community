package com.wzh.community.config;

import com.wzh.community.controller.interceptor.LoginRequiredInterceptor;
import com.wzh.community.controller.interceptor.LoginTicketInterceptor;
import com.wzh.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author wzh
 * @data 2023/5/6 -22:23
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer{

        @Autowired
        private LoginTicketInterceptor loginTicketInterceptor;

        @Autowired
        private LoginRequiredInterceptor loginRequiredInterceptor;

        @Autowired
        private MessageInterceptor messageInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {

            registry.addInterceptor(loginTicketInterceptor)
                    .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.html");

            registry.addInterceptor(loginRequiredInterceptor)
                    .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.html");

            registry.addInterceptor(messageInterceptor)
                    .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.html");
        }
}
