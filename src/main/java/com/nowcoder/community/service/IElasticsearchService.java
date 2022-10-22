package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.SearchPage;

public interface IElasticsearchService
{
    public void saveDiscussPost(DiscussPost discussPost);

    public void deleteDiscussPost(int id);

    public SearchPage<DiscussPost> searchDiscussPost(String keyword, int current, int limit);
}
