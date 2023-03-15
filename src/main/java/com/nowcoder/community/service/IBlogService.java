package com.nowcoder.community.service;

import com.nowcoder.community.entity.Blog;

import java.util.List;

public interface IBlogService {

    public List<Blog> findBlogs(int userId, int offset, int limit, int orderMode);

    public int findBlogRows(int userId);

    public int addBlog(Blog blog);

    public Blog findBlogById(int id);

    public List<Blog> findBlogsByTag(String tag, int offset, int limit);

    public int findTagBlogRows(String tag);

    public int updateCommentCount(int id,int commentCount);

    public int updateType(int id,int type);

    public int updateStatus(int id,int status);

    public int updateScore(int id,double score);

}
