package com.nowcoder.community.service;

import com.nowcoder.community.entity.Blog;

import java.util.List;

public interface ILikeService {

    public void like(int userId,int entityType,int entityId,int entityUserId);

    public long findEntityLikeCount(int entityType,int entityId);

    public int findEntityLikeStatus(int userId,int entityType,int entityId);

    public int findUserLikeCount(int userId);

    public long findLikePostCount(int userId);

    public List<Blog> findLikePosts(int userId, int offset, int limit);

    public void deleteLikePosts(int id);
}
