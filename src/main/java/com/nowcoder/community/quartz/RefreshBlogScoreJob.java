package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.Blog;
import com.nowcoder.community.service.IBlogService;
import com.nowcoder.community.service.IElasticsearchService;
import com.nowcoder.community.service.ILikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RefreshBlogScoreJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(RefreshBlogScoreJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IBlogService blogService;

    @Autowired
    private ILikeService likeService;

    @Autowired
    private IElasticsearchService elasticsearchService;

    /** 创建纪元 */
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-09-23 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化博客纪元失败!",e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if(operations.size() == 0){
            logger.info("[任务取消] 没有要刷新的帖子!");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数: " + operations.size());

        while(operations.size() > 0){
            this.refresh((Integer) operations.pop());
        }

        logger.info("[任务结束] 帖子分数刷新完毕!");
    }

    private void refresh(int blogId){
        Blog blog =  blogService.findBlogById(blogId);

        if(blog == null){
            logger.error("该帖子不存在: id = " + blogId);
            return;
        }

        /** 是否精华 */
        boolean wonderful = blog.getStatus() == 1;
        /** 评论数量 */
        int commentCount = blog.getCommentCount();
        /** 点赞数量 */
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_BLOG,blogId);

        /** 计算权重 */
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        /** 分数 = 帖子权重 + 距离天数 */
        double score = Math.log10(Math.max(w,1)) +
                (blog.getCreateTime().getTime() - epoch.getTime())/(1000 * 3600 * 24);
        /** 更新帖子分数 */
        blogService.updateScore(blogId,score);
        /** 同步搜索数据 */
        blog.setScore(score);
        elasticsearchService.saveBlog(blog);

    }
}
