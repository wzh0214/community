package com.wzh.community.controller;

import com.wzh.community.entity.Page;
import com.wzh.community.entity.DiscussPost;
import com.wzh.community.entity.User;
import com.wzh.community.service.impl.DiscussServerImpl;
import com.wzh.community.service.impl.LikeServerImpl;
import com.wzh.community.service.impl.UserServerImpl;
import com.wzh.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wang
 * @date 2022/7/28 - 19:24
 */
@Controller
public class HomeController {
    @Autowired
    private DiscussServerImpl discussServer;

    @Autowired
    private UserServerImpl userServer;

    @Autowired
    private LikeServerImpl likeServer;

    @GetMapping("/")
    public String root() {
        return "forward:/index";
    }

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int ordermMode) {
        // springMVC会自动实例化Model和Page，并将page注入model，所以thymeleaf可以直接访问Page对象数据
        page.setRows(discussServer.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + ordermMode);

        List<DiscussPost> list =  discussServer.findDiscussPosts(0, page.getOffset(), page.getLimit(), ordermMode);
        ArrayList<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userServer.findUserById(post.getUserId());
                map.put("user", user);

                // 查看每个帖子点赞的总数
                long likeCount = likeServer.findEntityLikeCount((int) CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue(), post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", ordermMode);
        return "/index";
    }


    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }


}
