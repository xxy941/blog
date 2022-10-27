package com.nowcoder.community.service;

import java.util.List;
import java.util.Map;

public interface IFollowService {

    public void follow(int userId,int entityType,int entityId);

    public void unfollow(int userId,int entityType,int entityId);

    public long findFolloweeCount(int userId,int entityType);

    public long findFollowerCount(int userId,int entityType);

    public boolean hasFollowed(int userId,int entityType,int entityId);

    public List<Map<String,Object>> findFollowees(int userId, int offset, int limit);

    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit);

}
