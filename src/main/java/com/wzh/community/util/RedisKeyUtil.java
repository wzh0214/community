package com.wzh.community.util;

/**
 * @author wzh
 * @data 2022/8/10 -18:16
 * 设置key名的工具类，方便复用
 */
public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    public static final String PREFIX_USER_LIKE = "like:user";
    // 若A关注了B，则A是B的Follower(粉丝)， 则B是A的Followee(目标)
    public static final String PREFIX_FOLLOWEE = "followee"; // 目标
    public static final String PREFIX_FOLLOWER = "follower"; // 粉丝
    public static final String PREFIX_KAPTCHA = "kaptcha";
    public static final String PREFIX_TICKET = "ticket";
    public static final String PREFIX_USER = "user";
    public static final String PREFIX_UV = "uv"; // 访客
    public static final String PREFIX_DAU = "dau"; // 活跃人数
    public static final String PREFIX_POST = "post";



    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId) 设置为set类型存的是用户id，如果要看谁给我点的赞
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE +  SPLIT + userId;
    }

    // 某个用户关注的实体
    // followee:userId:entityType -> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT  + entityId;
    }

    // 登陆验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登陆凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日uv
    public static String getUVKey(String data) {
        return PREFIX_UV + SPLIT + data;
    }

    // 区间uv
    public static String getUVKey(String startData, String endData) {
        return PREFIX_UV + SPLIT + startData + SPLIT + endData;
    }

    // 单日活跃用户
    public static String getDAUKey(String data) {
        return PREFIX_DAU + SPLIT + data;
    }

    // 区间dau
    public static String getDAUKey(String startData, String endData) {
        return PREFIX_DAU + SPLIT + startData + SPLIT + endData;
    }


    // 帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
