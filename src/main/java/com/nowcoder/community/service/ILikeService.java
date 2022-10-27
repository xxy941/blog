package com.nowcoder.community.service;

public interface ILikeService {

    public void like(int userId,int entityType,int entityId,int entityUserId);

    public long findEntityLikeCount(int entityType,int entityId);

    public int findEntityLikeStatus(int userId,int entityType,int entityId);

    public int findUserLikeCount(int userId);
}
