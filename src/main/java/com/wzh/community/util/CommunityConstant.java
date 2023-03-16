package com.wzh.community.util;

/**
 * @author wzh
 * @data 2022/7/31 -21:11
 */
public enum CommunityConstant {
    /**
     * 激活成功
     * 重复激活
     * 激活失败
     *
     * 记住登陆
     * 默认登陆
     *
     * 实体类型：帖子  评论  用户
     */
    ACTIVATION_SUCCESS, ACTIVATION_REPEAT, ACTIVATION_FAILURE,
    REMEMBER_EXPIRED_SECONDS(3600 * 24 * 30), DEFAULT_EXPIRED_SECONDS(3600 * 12),
    ENTITY_TYPE_POST(1), ENTITY_TYPE_COMMENT(2),ENTITY_TYPE_USER(3);




    long value;

    CommunityConstant(long value) {
        this.value = value;
    }

    CommunityConstant() {
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
