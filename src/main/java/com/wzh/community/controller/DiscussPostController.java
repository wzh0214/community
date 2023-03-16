package com.wzh.community.controller;

import com.wzh.community.entity.*;
import com.wzh.community.event.EventProducer;
import com.wzh.community.service.impl.CommentServerImpl;
import com.wzh.community.service.impl.DiscussServerImpl;
import com.wzh.community.service.impl.LikeServerImpl;
import com.wzh.community.service.impl.UserServerImpl;
import com.wzh.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wzh
 * @data 2022/8/5 -14:43
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements EventConstant {
    @Autowired
    private DiscussServerImpl discussServer;

    @Autowired
    private UserServerImpl userServer;

    @Autowired
    private CommentServerImpl commentServer;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeServerImpl likeServer;

    @Autowired
    private EventProducer eventProducer;
    
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 异步请求所以返回的是String，不是页面
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    // 要和实体类的属性相同，不然为null，导致报错
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登陆");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());

        discussServer.addDiscussPost(post);

        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType((int) CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue())
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        // 报错的情况，将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功!");

    }

    @GetMapping("/detail/{id}")
    public String getDiscussPost(@PathVariable("id") int id, Model model, Page page) {
        //没有用多表联查，查出用户的信息，而是通过post的UserId查用户，虽然效率比多表联查低，但之后可以用把用户信息存redis，提高效率
        DiscussPost post = discussServer.findDiscussPostById(id);
        model.addAttribute("post", post);

        User user = userServer.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 点赞数量
        long likeCount = likeServer.findEntityLikeCount((int) CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue(), id);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态，如果用户没登陆返回0
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeServer.findEntityLikeStatus(hostHolder.getUser().getId(), (int) CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue(), id);
        model.addAttribute("likeStatus", likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + id);
        // post中有帖子回复总数字段
        page.setRows(post.getCommentCount());


        // 评论：给帖子的评论
        // 回复：给评论的评论
        // commentVoList：评论列表
        List<Comment> commentList = commentServer.findCommentsByEntity((int) CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue(),
                post.getId(), page.getOffset(), page.getLimit());
        // 评论vo列表，vo:详情
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论vo
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 评论作者
                commentVo.put("user", userServer.findUserById(comment.getUserId()));
                // 点赞数量
                likeCount = likeServer.findEntityLikeCount((int) CommunityConstant.valueOf("ENTITY_TYPE_COMMENT").getValue(), comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态，如果用户没登陆返回0
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeServer.findEntityLikeStatus(hostHolder.getUser().getId(), (int) CommunityConstant.valueOf("ENTITY_TYPE_COMMENT").getValue(), comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 回复列表
                List<Comment> replyList = commentServer.findCommentsByEntity((int) CommunityConstant.valueOf("ENTITY_TYPE_COMMENT").getValue(),
                        comment.getId(), 0, Integer.MAX_VALUE);
                // 回复vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userServer.findUserById(reply.getUserId()));
                        // 点赞数量
                        likeCount = likeServer.findEntityLikeCount((int) CommunityConstant.valueOf("ENTITY_TYPE_COMMENT").getValue(), reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态，如果用户没登陆返回0
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeServer.findEntityLikeStatus(hostHolder.getUser().getId(), (int) CommunityConstant.valueOf("ENTITY_TYPE_COMMENT").getValue(), reply.getId());
                        replyVo.put("likeStatus", likeStatus);
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userServer.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentServer.findCommentCount((int) CommunityConstant.valueOf("ENTITY_TYPE_COMMENT").getValue(), comment.getId());
                commentVo.put("replyCount", replyCount);


                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";


    }


    // 置顶
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id) {
        discussServer.updateType(id, 1);

        // 触发事件，同步到es
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType((int) CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue())
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);

    }


    // 加精
    @PostMapping("/wouderful")
    @ResponseBody
    public String setWouderful(int id) {
        discussServer.updateStatus(id, 1);

        // 触发事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType((int) CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue())
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);


        return CommunityUtil.getJSONString(0);

    }

    // 删除
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {
        discussServer.updateStatus(id, 2);

        // 触发删除事件，从es中删除帖子
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType((int) CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue())
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);

    }


}
