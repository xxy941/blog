package com.nowcoder.community.mapper;

import com.nowcoder.community.dao.BlogMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.Blog;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearchTests {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticSearchRestTemplate;

    @Test
    public void testInsert(){
        discussPostRepository.save(blogMapper.selectBlogById(241));
        discussPostRepository.save(blogMapper.selectBlogById(242));
        discussPostRepository.save(blogMapper.selectBlogById(243));
    }

    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(blogMapper.selectBlogs(101,0,100,0));
        discussPostRepository.saveAll(blogMapper.selectBlogs(102,0,100,0));
        discussPostRepository.saveAll(blogMapper.selectBlogs(103,0,100,0));
        discussPostRepository.saveAll(blogMapper.selectBlogs(111,0,100,0));
        discussPostRepository.saveAll(blogMapper.selectBlogs(112,0,100,0));
        discussPostRepository.saveAll(blogMapper.selectBlogs(131,0,100,0));
        discussPostRepository.saveAll(blogMapper.selectBlogs(132,0,100,0));
        discussPostRepository.saveAll(blogMapper.selectBlogs(133,0,100,0));
        discussPostRepository.saveAll(blogMapper.selectBlogs(134,0,100,0));
    }

    @Test
    public void testUpdate(){
        Blog blog = blogMapper.selectBlogById(231);
        blog.setContent("我是新人，使劲灌水");
        discussPostRepository.save(blog);
    }

    @Test
    public void testDelete(){
        //discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository(){
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();


        SearchHits<Blog> search = elasticSearchRestTemplate.search(build, Blog.class);
        SearchPage<Blog> page = SearchHitSupport.searchPageFor(search, build.getPageable());
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (SearchHit<Blog> discussPostSearchHit : page) {
            System.out.println("高亮内容"+discussPostSearchHit.getHighlightFields()); //高亮内容
            System.out.println("原始内容"+discussPostSearchHit.getContent()); //原始内容
        }
        //下面是封装将高亮部分成一个page对象，也可以不做，直接discussPostSearchHit.getHighlightFields()获取
        List<Blog> list = new ArrayList<>();
        for (SearchHit<Blog> discussPostSearchHit : page) {
            Blog blog = discussPostSearchHit.getContent();
            //discussPostSearchHit.getHighlightFields() //高亮
            if (discussPostSearchHit.getHighlightFields().get("title") != null) {
                blog.setTitle(discussPostSearchHit.getHighlightFields().get("title").get(0));
            }
            if (discussPostSearchHit.getHighlightFields().get("content") != null) {
                blog.setContent(discussPostSearchHit.getHighlightFields().get("content").get(0));
            }
            //System.out.println(discussPostSearchHit.getContent());
            list.add(blog);
        }

        for (Blog blog : list) {
            System.out.println("123 " + blog + " 321");
        }

        PageImpl<Blog> pageInfo = new PageImpl<Blog>(list, build.getPageable(), search.getTotalHits());
        System.out.println(pageInfo.getTotalElements());
        System.out.println(pageInfo.getTotalPages());
        System.out.println(pageInfo.getNumber());
        System.out.println(pageInfo.getSize());
        for (Blog blog : pageInfo) {
            System.out.println(blog);
        }


    }


}
