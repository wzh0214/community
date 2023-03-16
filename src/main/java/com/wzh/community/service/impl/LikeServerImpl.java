package com.wzh.community.service.impl;

import com.wzh.community.service.LikeServer;
import com.wzh.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @author wzh
 * @data 2022/8/10 -18:34
 */
@Service
public class LikeServerImpl implements LikeServer {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    // 点赞
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        // 因为是两次操作，要保证事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId); // 用来个人页面显示用户收到的赞

                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                operations.multi();

                // 判断是否已经点过赞，如果点过再点就是取消赞，没点过就创建增加
                if (isMember) {
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });


    }

    @Override
    // 查询某实体被点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size((entityLikeKey));
    }


    @Override
    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 1代表已赞，0代表没点赞，如果将来开发点踩可以返回-1
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }


    @Override
    // 查询某个用户获得的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }

}
