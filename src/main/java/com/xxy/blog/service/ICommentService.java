package com.xxy.blog.service;

import com.xxy.blog.entity.Comment;

import java.util.List;

public interface ICommentService {
    public List<Comment> findCommentsById(int entityType, int entityId, int offset, int limit);

    public int findCommentCount(int entityType,int entityId);

    public int addComment(Comment comment);

    public Comment findCommentById(int id);

}
