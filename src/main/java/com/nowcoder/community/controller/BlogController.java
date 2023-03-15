package com.nowcoder.community.controller;


import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.ICommentService;
import com.nowcoder.community.service.IBlogService;
import com.nowcoder.community.service.ILikeService;
import com.nowcoder.community.service.IUserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Controller
@RequestMapping("/blog")
public class BlogController implements CommunityConstant {

    @Autowired
    private IBlogService blogService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private IUserService userService;

    @Autowired
    private ICommentService commentService;

    @Autowired
    private ILikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addBlog(String title, String content, String tag){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"你还没有登录哦!");
        }

        Blog post = new Blog();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setTag(tag);
        blogService.addBlog(post);

        /** 触发发帖事件 */
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_BLOG)
                .setEntityId(post.getId());

        eventProducer.fireEvent(event);

        /** 计算发帖分数 */
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,post.getId());

        /** 报错的情况将来统一处理 */
        return CommunityUtil.getJSONString(0,"发布成功!");
    }

    @RequestMapping("/classify/{tag}")
    public String getClassifyBlogs(Model model, Page page, @PathVariable("tag") String tag){
        page.setPath("/blog/classify/" + tag);
        page.setRows(blogService.findTagBlogRows(tag));
        List<Blog> blogs = blogService.findBlogsByTag(tag,page.getOffset(), page.getLimit());
        List<Map<String,Object>> tagBlogs = new ArrayList<>();
        if(blogs != null){
            for (Blog blog : blogs) {
                Map<String,Object> map = new HashMap<>();
                map.put("blog", blog);
                map.put("user",userService.findUserById(blog.getUserId()));
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_BLOG, blog.getId()));
                tagBlogs.add(map);
            }
        }
        model.addAttribute("tag",tag);
        model.addAttribute("blogs",tagBlogs);
        return "/site/classify-page";
    }

    @RequestMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        Blog post = blogService.findBlogById(discussPostId);
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_BLOG,discussPostId);
        long likeCount = 0;
        model.addAttribute("post",post);
        model.addAttribute("user",userService.findUserById(post.getUserId()));
        model.addAttribute("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_BLOG,discussPostId));
        model.addAttribute("likeStatus",likeStatus);

        page.setLimit(5);
        page.setPath("/blog/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_BLOG,post.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if(commentVoList != null){
            for (Comment comment : commentList) {
                /** 评论VO */
                Map<String,Object> commentVo = new HashMap<>();
                /** 评论 */
                commentVo.put("comment",comment);
                /** 作者 */
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                /** 点赞数量 */
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                /** 点赞状态 */
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);

                /** 回复列表 */
                List<Comment> replyList =
                commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(),0,Integer.MAX_VALUE);
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for (Comment reply : replyList) {
                        Map<String,Object> replyVo = new HashMap<>();
                        /** 回复 */
                        replyVo.put("reply",reply);
                        /** 作者 */
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        /** 回复目标 */
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        /** 点赞数量 */
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        /** 点赞状态 */
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);

            }
        }
        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }

    /** 置顶 */
    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        blogService.updateType(id,1);

        /** 触发发帖事件 */
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_BLOG)
                .setEntityId(id);

        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    /** 加精 */
    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        blogService.updateStatus(id,1);

        /** 触发发帖事件 */
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_BLOG)
                .setEntityId(id);

        eventProducer.fireEvent(event);

        /** 计算发帖分数 */
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        return CommunityUtil.getJSONString(0);
    }

    /** 删除 */
    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        blogService.updateStatus(id,2);
        likeService.deleteLikePosts(id);
        /** 触发发帖事件 */
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_BLOG)
                .setEntityId(id);

        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
