package com.nowcoder.community.service;

import com.nowcoder.community.entity.Comment;

import java.util.List;

public interface ICommentService {
    public List<Comment> findCommentsByEntity(int entityType,int entityId,int offset,int limit);

    public int findCommentCount(int entityType,int entityId);

    public int addComment(Comment comment);
}
