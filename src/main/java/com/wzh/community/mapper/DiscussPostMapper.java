package com.wzh.community.mapper;

import com.wzh.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wang
 * @date 2022/7/28 - 16:27
 */
@Mapper
public interface DiscussPostMapper {
    // offset每一页的行号，limit每页显示的数据，userId如果没传的化就是查所有的帖子，如果传了的化就是看自己发的帖子
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    // @param注解用于给参数取别名
    // 查询帖子数
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    // 当有用户评论后，要增加评论总数
    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);
}
