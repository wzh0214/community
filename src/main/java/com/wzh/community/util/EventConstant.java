package com.wzh.community.util;

/**
 * @author wzh
 * @data 2022/8/18 -19:17
 */
public interface EventConstant {
    // 点赞
    String TOPIC_LIKE = "like";

    // 评论
    String TOPIC_COMMENT = "comment";

    // 关注
    String TOPIC_FOLLOW = "follow";

    // 系统用户id
    int SYSTEM_USER_ID = 1;


    // 主题:发帖
    String TOPIC_PUBLISH ="publish";

    // 主题：删除
    String TOPIC_DELETE = "delete";

    // 权限：普通用户
    String AUTHORITY_USER = "user";


    // 权限：管理员
    String AUTHORITY_ADMIN = "admin";

    // 权限：版主
    String AUTHORITY_MODERATOR = "moderator";





}
