package com.wzh.community.service;

/**
 * @author wzh
 * @data 2022/8/10 -19:05
 */
public interface LikeServer {
    void like(int userId, int entityType, int entityId, int entityUserId);

    long findEntityLikeCount(int entityType, int entityId);

    int findEntityLikeStatus(int userId, int entityType, int entityId);

    int findUserLikeCount(int userId);
}
