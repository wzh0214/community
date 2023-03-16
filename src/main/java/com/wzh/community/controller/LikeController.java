package com.wzh.community.controller;

import com.wzh.community.entity.Event;
import com.wzh.community.entity.User;
import com.wzh.community.event.EventProducer;
import com.wzh.community.service.impl.LikeServerImpl;
import com.wzh.community.util.*;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wzh
 * @data 2022/8/10 -19:06
 */
@Controller
public class LikeController implements EventConstant {
    @Autowired
    private LikeServerImpl likeServer;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        // 点赞
        likeServer.like(user.getId(), entityType, entityId, entityUserId);
        // 数量
        long likeCount = likeServer.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeServer.findEntityLikeStatus(user.getId(), entityType, entityId);

        // 返回结果
        Map<String ,Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);

        }


        // 如果是对帖子点赞计算帖子分数
        if (entityType == CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue()) {
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        // 异步请求，返回json
        return CommunityUtil.getJSONString(0, null, map);


    }
}
