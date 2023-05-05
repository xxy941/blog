package com.xxy.blog.service.impl;

import com.xxy.blog.dao.CommentMapper;
import com.xxy.blog.service.IBlogService;
import com.xxy.blog.service.ICommentService;
import com.xxy.blog.util.CommunityConstant;
import com.xxy.blog.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements ICommentService, CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private IBlogService discussPostService;

    @Override
    public List<Comment> findCommentsById(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    @Override
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空!");
        }

        /** 添加评论 */
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        /** 更新帖子评论数据 */
        if(comment.getEntityType() == ENTITY_TYPE_BLOG){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }

        return rows;
    }

    @Override
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
