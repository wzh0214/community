package com.wzh.community.service;

import com.wzh.community.entity.Comment;

import java.util.List;

/**
 * @author wzh
 * @data 2022/8/5 -20:54
 */
public interface CommentServer {
    List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int findCommentCount(int entityType, int entityId);

    int addComment(Comment comment);
}
