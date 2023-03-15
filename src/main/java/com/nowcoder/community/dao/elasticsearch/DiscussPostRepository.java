package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.Blog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<Blog,Integer> {

}
