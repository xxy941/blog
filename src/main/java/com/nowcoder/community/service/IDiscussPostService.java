package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;

import java.util.List;

public interface IDiscussPostService {

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode);

    public int findDiscussPostRows(int userId);

    public int addDiscussPost(DiscussPost post);

    public DiscussPost findDiscussPostById(int id);

    public int updateCommentCount(int id,int commentCount);

    public int updateType(int id,int type);

    public int updateStatus(int id,int status);

    public int updateScore(int id,double score);

}
