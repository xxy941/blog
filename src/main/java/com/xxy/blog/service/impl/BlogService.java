package com.xxy.blog.service.impl;

import com.xxy.blog.dao.BlogMapper;
import com.xxy.blog.entity.Blog;
import com.xxy.blog.service.IBlogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;


import java.util.List;

@Service
public class BlogService implements IBlogService {

    private static final Logger logger = LoggerFactory.getLogger(BlogService.class);

    @Autowired
    private BlogMapper blogMapper;

    @Override
    public List<Blog> findBlogs(int userId, int offset, int limit, int orderMode) {
        return blogMapper.selectBlogs(userId,offset,limit,orderMode);
    }

    @Override
    public int findBlogRows(int userId) {
        return blogMapper.selectBlogRows(userId);
    }

    @Override
    public int addBlog(Blog blog) {
        if(blog == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        /** 转义HTML标签 */
        blog.setTitle(HtmlUtils.htmlEscape(blog.getTitle()));
        blog.setContent(HtmlUtils.htmlEscape(blog.getContent()));

        return blogMapper.insertBlog(blog);
    }

    @Override
    public Blog findBlogById(int id) {
        return blogMapper.selectBlogById(id);
    }

    @Override
    public List<Blog> findBlogsByTag(String tag, int offset, int limit) {
        return blogMapper.selectBlogsByTag(tag, offset, limit);
    }

    @Override
    public List<Blog> findBlogsByKeyword(String keyword, int offset, int limit) {
        return blogMapper.selectBlogsByKeyword("%" + keyword + "%",offset,limit);
    }

    @Override
    public int findTagBlogRows(String tag) {
        return blogMapper.selectTagBlogRows(tag);
    }

    @Override
    public List<Blog> findAllBlogs() {
        return blogMapper.selectAllBlogs();
    }

    @Override
    public int updateCommentCount(int id, int commentCount) {
        return blogMapper.updateCommentCount(id,commentCount);
    }

    @Override
    public int updateType(int id, int type) {
        return blogMapper.updateType(id, type);
    }

    @Override
    public int updateStatus(int id, int status) {
        return blogMapper.updateStatus(id, status);
    }

    @Override
    public int updateScore(int id, double score) {
        return blogMapper.updateScore(id,score);
    }

    @Override
    public int updateBlog(int id, String title, String tag, String content) {
        return blogMapper.updateBlog(id,title,tag,content);
    }
}
