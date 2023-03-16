package com.wzh.community.service;

/**
 * @author wzh
 * @data 2022/8/12 -15:50
 */
public interface FollowServer {
    void follow(int userId, int entityType, int entityId);

    void unfollow(int userId, int entityType, int entityId);
}
