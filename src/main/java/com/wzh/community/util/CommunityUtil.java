package com.wzh.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;


import java.util.Map;
import java.util.UUID;

/**
 * @author wzh
 * @data 2022/7/31 -13:25
 */
public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    // 密码+随机字符串 加密安全性更高
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) { // commons.lang3工具类，判断是否为null,空字符串，空格字符串
            return null;
        }

        return DigestUtils.md5DigestAsHex(key.getBytes());
    }


    // 事先要在pom文件中加fastJSON包
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        // 把数据传入json对象，以String类型返回
        JSONObject json = new JSONObject();

        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }

        return json.toJSONString();
    }

    // 重载方法
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }



}
