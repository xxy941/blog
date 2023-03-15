package com.nowcoder.community.service;

import com.nowcoder.community.entity.Blog;
import org.springframework.data.elasticsearch.core.SearchPage;

public interface IElasticsearchService
{
    public void saveBlog(Blog blog);

    public void deleteBlog(int id);

    public SearchPage<Blog> searchBlog(String keyword, int current, int limit);
}
