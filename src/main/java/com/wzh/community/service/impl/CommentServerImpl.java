package com.wzh.community.service.impl;

import com.wzh.community.entity.Comment;
import com.wzh.community.mapper.CommentMapper;
import com.wzh.community.service.CommentServer;
import com.wzh.community.util.CommunityConstant;
import com.wzh.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author wzh
 * @data 2022/8/5 -20:54
 */
@Service
public class CommentServerImpl implements CommentServer {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;
    
    @Autowired
    private DiscussServerImpl discussServer;

    @Override
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    @Override
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    // 多线程下存在隐患，可以改为synchronized
    // REQUIRED：支持当前事务(外部事务),如果不存在则创建新事务
    // REQUIRES_NEW:创建一个新事务，并且暂停当前事务(外部事务)
    // NESTED：如果当前存在事务(外部事务),则嵌套再该事务执行，否则和REQUIRED一样
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量
        if (comment.getEntityType() == CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue()) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussServer.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;

    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
