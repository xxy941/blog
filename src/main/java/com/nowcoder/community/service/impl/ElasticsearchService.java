package com.nowcoder.community.service.impl;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.Blog;
import com.nowcoder.community.service.IElasticsearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticsearchService implements IElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public void saveBlog(Blog blog) {
        discussPostRepository.save(blog);
    }

    @Override
    public void deleteBlog(int id) {
        discussPostRepository.deleteById(id);
    }

    @Override
    public SearchPage<Blog> searchBlog(String keyword, int current, int limit) {
        NativeSearchQuery searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        (SortBuilders.fieldSort("score").order(SortOrder.DESC)),
                        (SortBuilders.fieldSort("createTime").order(SortOrder.DESC)))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        //得到查询结果
        SearchHits<Blog> search = elasticsearchRestTemplate.search(searchQueryBuilder, Blog.class);
        //将其结果返回并进行分页
        SearchPage<Blog> page = SearchHitSupport.searchPageFor(search, Page.empty().getPageable());

        if (!page.isEmpty()) {
            for (SearchHit<Blog> discussPostSearch : page) {
                Blog blog = discussPostSearch.getContent();
                //取高亮
                List<String> title = discussPostSearch.getHighlightFields().get("title");
                if(title!=null){
                    blog.setTitle(title.get(0));
                }
                List<String> content = discussPostSearch.getHighlightFields().get("content");
                if(content!=null){
                    blog.setContent(content.get(0));
                }
            }
        }

        return page;
    }
}
