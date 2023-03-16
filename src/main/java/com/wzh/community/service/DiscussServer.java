package com.wzh.community.service;

import com.wzh.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wang
 * @date 2022/7/28 - 19:02
 */
public interface DiscussServer {
    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderModel);

    int findDiscussPostRows(@Param("userId") int userId);

    int addDiscussPost(DiscussPost post);

    DiscussPost findDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);
}
