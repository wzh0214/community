package com.wzh.community.controller;

import com.wzh.community.annotation.LoginRequired;
import com.wzh.community.entity.Comment;
import com.wzh.community.entity.DiscussPost;
import com.wzh.community.entity.Event;
import com.wzh.community.event.EventProducer;
import com.wzh.community.service.impl.CommentServerImpl;
import com.wzh.community.service.impl.DiscussServerImpl;
import com.wzh.community.util.CommunityConstant;
import com.wzh.community.util.EventConstant;
import com.wzh.community.util.HostHolder;
import com.wzh.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author wzh
 * @data 2022/8/6 -20:40
 */
@Controller
@RequestMapping("/comment")
public class
CommentController implements EventConstant {
    @Autowired
    private CommentServerImpl commentServer;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussServerImpl discussServer;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;


    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        // 如果用户没登陆，会有异常，后面会统一处理
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentServer.addComment(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        if (comment.getEntityType() == CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue()) {
            DiscussPost target = discussServer.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == CommunityConstant.valueOf("ENTITY_TYPE_COMMENT").getValue()) {
            Comment target = commentServer.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        // 触发发帖事件
        if (comment.getEntityType() == CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue()) {
             event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType((int)CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue())
                    .setEntityId(discussPostId);

            eventProducer.fireEvent(event);
        }

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPostId);

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
