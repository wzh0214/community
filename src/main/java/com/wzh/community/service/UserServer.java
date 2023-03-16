package com.wzh.community.service;

import com.wzh.community.entity.LoginTicket;
import com.wzh.community.entity.User;
import com.wzh.community.util.CommunityConstant;

import java.util.Map;

/**
 * @author wang
 * @date 2022/7/28 - 19:06
 */
public interface UserServer {
    User findUserById(int id);

    Map<String, Object> register(User user);

    CommunityConstant activation(int userId, String code);

    Map<String, Object> login(String username, String password, long expiredSeconds);

    void logout(String ticket);

    LoginTicket findLoginTicket(String ticket);

    int updateHeader(int userId, String headerUrl);

    public Map<String, Object> updatePassword(int id, String oldPassword, String newPassword);

    User findUserByName(String userName);
}
