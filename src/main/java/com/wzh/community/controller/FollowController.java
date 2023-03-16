package com.wzh.community.controller;

import com.wzh.community.entity.Event;
import com.wzh.community.entity.Page;
import com.wzh.community.entity.User;
import com.wzh.community.event.EventProducer;
import com.wzh.community.service.impl.FollowServerImpl;
import com.wzh.community.service.impl.UserServerImpl;
import com.wzh.community.util.CommunityConstant;
import com.wzh.community.util.CommunityUtil;
import com.wzh.community.util.EventConstant;
import com.wzh.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author wzh
 * @data 2022/8/12 -15:51
 */
@Controller
public class FollowController implements EventConstant {
    @Autowired
    private FollowServerImpl followServer;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserServerImpl userServer;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followServer.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);

        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注！");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followServer.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注！");
    }


    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userServer.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 因为页面上要显示来自谁的关注和谁的粉丝
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees" + userId);
        page.setRows((int) followServer.findFolloweeCount(userId, (int)CommunityConstant.valueOf("ENTITY_TYPE_USER").getValue()));

        List<Map<String, Object>> userList = followServer.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User)map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));

            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userServer.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 因为页面上要显示来自谁的关注和谁的粉丝
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers" + userId);
        page.setRows((int) followServer.findFollowerCount((int)CommunityConstant.valueOf("ENTITY_TYPE_USER").getValue(), userId));

        List<Map<String, Object>> userList = followServer.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User)map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));

            }
        }
        model.addAttribute("users", userList);

        return "/site/follower";
    }

    // 判断当前用户是否关注列表中的人
    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }

        return followServer.hasFollowed(hostHolder.getUser().getId(), (int) CommunityConstant.valueOf("ENTITY_TYPE_USER").getValue(), userId);
    }
}
