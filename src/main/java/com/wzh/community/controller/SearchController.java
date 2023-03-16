package com.wzh.community.controller;

import com.wzh.community.entity.DiscussPost;
import com.wzh.community.entity.Page;
import com.wzh.community.entity.SearchResult;
import com.wzh.community.service.LikeServer;
import com.wzh.community.service.impl.ElasticSearchServiceImpl;
import com.wzh.community.service.impl.UserServerImpl;
import com.wzh.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wzh
 * @data 2022/9/21 -19:58
 */
@Controller
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    @Autowired
    private ElasticSearchServiceImpl elasticSearchService;

    @Autowired
    private UserServerImpl userServer;

    @Autowired
    private LikeServer likeServer;


    // search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {


        //搜索帖子
        try {
            SearchResult searchResult = elasticSearchService.searchDiscussPost(keyword, (page.getCurrent() - 1)*10, page.getLimit());
            List<Map<String,Object>> discussPosts = new ArrayList<>();
            List<DiscussPost> list = searchResult.getList();
            if(list != null) {
                for (DiscussPost post : list) {
                    Map<String,Object> map = new HashMap<>();
                    //帖子
                    map.put("post",post);
                    // 作者
                    map.put("user",userServer.findUserById(post.getUserId()));
                    // 点赞数目
                    map.put("likeCount",likeServer.findEntityLikeCount((int) CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue(), post.getId()));

                    discussPosts.add(map);
                }
            }
            model.addAttribute("discussPosts",discussPosts);
            model.addAttribute("keyword",keyword);
            //分页信息
            page.setPath("/search?keyword=" + keyword);
            page.setRows(searchResult.getTotal() == 0 ? 0 : (int) searchResult.getTotal());

        } catch (IOException e) {
            logger.error("系统出错，没有数据：" + e.getMessage());
        }
        return "/site/search";
    }

}
