package com.xxy.blog.quartz;

import com.xxy.blog.entity.Blog;
import com.xxy.blog.service.IBlogService;
import com.xxy.blog.service.ILikeService;
import com.xxy.blog.util.CommunityConstant;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class RefreshBlogScoreJob implements Job, CommunityConstant {
    @Autowired
    private IBlogService blogService;

    @Autowired
    private ILikeService likeService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        List<Blog> blogs = blogService.findAllBlogs();
        for (Blog blog : blogs) {
            double likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_BLOG, blog.getId());
            double score = likeCount + 2 * blog.getCommentCount();
            blogService.updateScore(blog.getId(), score);
        }
    }
}
