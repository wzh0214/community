package com.wzh.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wzh
 * @data 2022/8/3 -19:34
 * 只要带有当前自定义注解的方法，通过拦截器判断在没登陆的情况下就不能访问带有这个注解的方法
 * 起到标识作用
 *
 * 引入springsercuity后就没必要用了
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}
