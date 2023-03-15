package com.nowcoder.community.service.impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.BlogMapper;
import com.nowcoder.community.entity.Blog;
import com.nowcoder.community.service.IBlogService;
import com.nowcoder.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BlogService implements IBlogService {

    private static final Logger logger = LoggerFactory.getLogger(BlogService.class);

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    /** Caffeine核心接口: Cache,LoadingCache,AsyncLoadingCache */

    /** 帖子列表缓存 */
    private LoadingCache<String,List<Blog>> postListCache;

    /** 帖子总数缓存 */
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        /** 初始化帖子列表缓存 */
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<Blog>>() {
                    @Override
                    public @Nullable List<Blog> load(String key) throws Exception {
                        if(key == null || key.length() == 0){
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if(params == null || params.length != 2){
                            throw new IllegalArgumentException("参数错误!");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        /** 二级缓存:Redis -> mysql */



                        logger.error("load post list from DB.");
                        return blogMapper.selectBlogs(0,offset,limit,1);
                    }
                });
        /** 初始化帖子总数缓存 */
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer key) throws Exception {
                        logger.debug("load post rows from DB.");
                        return blogMapper.selectBlogRows(key);
                    }
                });
    }

    @Override
    public List<Blog> findBlogs(int userId, int offset, int limit, int orderMode) {
        if(userId == 0 && orderMode == 1){
            return postListCache.get(offset + ":" + limit);
        }

        logger.error("load post list from DB.");
        return blogMapper.selectBlogs(userId,offset,limit,orderMode);
    }

    @Override
    public int findBlogRows(int userId) {
        if (userId == 0){
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB.");
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

        /** 过滤敏感词 */
        blog.setTitle(sensitiveFilter.filter(blog.getTitle()));
        blog.setContent(sensitiveFilter.filter(blog.getContent()));
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
    public int findTagBlogRows(String tag) {
        return blogMapper.selectTagBlogRows(tag);
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
}
