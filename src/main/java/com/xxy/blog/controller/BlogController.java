package com.xxy.blog.controller;

import com.xxy.blog.util.CommunityConstant;
import com.xxy.blog.util.CommunityUtil;
import com.xxy.blog.util.LoginUser;
import com.xxy.blog.entity.Blog;
import com.xxy.blog.entity.Comment;
import com.xxy.blog.entity.Page;
import com.xxy.blog.entity.User;
import com.xxy.blog.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private LoginUser loginUser;

    @Autowired
    private IUserService userService;

    @Autowired
    private ICommentService commentService;

    @Autowired
    private ILikeService likeService;

    @Autowired
    private IFollowService followService;

    @RequestMapping(path = "/newblog")
    public String enterNewBlog(){
        return "/page/newblog";
    }

    @RequestMapping(path = "/editblogpage/{id}/{userId}")
    public String enterEditBlog(@PathVariable("id")int id,@PathVariable("userId") int userId,Model model){
        if(loginUser.getUser().getType()<1&&userId != loginUser.getUser().getId())return "/error/500";
        Blog blog = blogService.findBlogById(id);
        User user = userService.findUserById(blog.getUserId());
        model.addAttribute("blog",blog);
        model.addAttribute("user",user);

        return "/page/editblog";
    }

    @RequestMapping(path = "/editblog")
    public String enterEditBlog(int id,String title,String tag,String content,Model model){
        blogService.updateBlog(id,title,tag,content);
        Blog blog = blogService.findBlogById(id);
        User user = userService.findUserById(blog.getUserId());
        model.addAttribute("user",user);
        model.addAttribute("blog",blog);
        return "/page/editblog";
    }

    @RequestMapping("/search")
    public String search(String keyword, Page page, Model model){
        /** 搜索帖子 */
        List<Blog> blogs = blogService.findBlogsByKeyword(keyword, page.getCurrent() - 1, page.getLimit());
        List<Map<String, Object>> searchBlogs = new ArrayList<Map<String, Object>>();
        if (blogs != null) {
            for (Blog blog : blogs) {
                Map<String, Object> map = new HashMap<>();
                if(blog.getContent().length()>50){
                    blog.setContent(blog.getContent().substring(0,50) +"...");
                }
                map.put("blog", blog);
                map.put("user", userService.findUserById(blog.getUserId()));
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_BLOG, blog.getId()));
                searchBlogs.add(map);
            }
        }
        model.addAttribute("blogs", searchBlogs);
        model.addAttribute("keyword", keyword);
        page.setPath("/search?keyword=" + keyword);
        page.setRows(blogs.size());
        return "/page/search";
    }

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    public String addBlog(String title, String content, String tag,Model model){
        User user = loginUser.getUser();
        if(user == null){
            return "/page/login";
        }
        if(StringUtils.isBlank(title)){
            model.addAttribute("titleMsg","标题不能为空!");
            return "/page/newblog";
        }
        if(StringUtils.isBlank(content)){
            model.addAttribute("contentMsg","内容不能为空!");
            return "/page/newblog";
        }
        if(title.length()>40){
            model.addAttribute("titleMsg","标题不能超过40字!");
            return "/page/newblog";
        }
        Blog post = new Blog();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setTag(tag);
        blogService.addBlog(post);
        return "forward:/index";
    }

    @RequestMapping("/classify/{tag}")
    public String getClassifyBlogs(Model model, Page page, @PathVariable("tag") String tag){
        page.setPath("/blog/classify/" + tag);
        page.setRows(blogService.findTagBlogRows(tag));
        page.setLimit(6);
        List<Blog> blogs = blogService.findBlogsByTag(tag,page.getOffset(), page.getLimit());
        List<Map<String,Object>> tagBlogs = new ArrayList<>();
        if(blogs != null){
            for (Blog blog : blogs) {
                Map<String,Object> map = new HashMap<>();
                if(blog.getContent().length()>50){
                    blog.setContent(blog.getContent().substring(0,50) +"...");
                }
                map.put("blog", blog);
                map.put("user",userService.findUserById(blog.getUserId()));
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_BLOG, blog.getId()));
                tagBlogs.add(map);
            }
        }
        model.addAttribute("tag",tag);
        model.addAttribute("blogs",tagBlogs);
        return "/page/classify-page";
    }


    @RequestMapping("/detail/{blogId}")
    public String getBlogById(@PathVariable("blogId") int blogId, Model model, Page page){
        Blog post = blogService.findBlogById(blogId);
        long likeCount = 0;
        long followeeCount = followService.findFolloweeCount(post.getUserId(),ENTITY_TYPE_USER);
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,post.getUserId());

        model.addAttribute("post",post);
        model.addAttribute("user",userService.findUserById(post.getUserId()));
        model.addAttribute("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_BLOG,blogId));
        model.addAttribute("followeeCount",followeeCount);
        model.addAttribute("followerCount",followerCount);

        page.setLimit(5);
        page.setPath("/blog/detail/" + blogId);
        page.setRows(post.getCommentCount());

        List<Comment> commentList = commentService.findCommentsById(1,post.getId(),page.getOffset(),page.getLimit());
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
                commentVoList.add(commentVo);

            }
        }
        model.addAttribute("comments",commentVoList);

        return "/page/blog-detail";
    }


    /** 删除 */
    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id,int userId){
        System.out.println(userId);
        User user = loginUser.getUser();
        if(userId!=user.getId()&&user.getType()!=1)return CommunityUtil.getJSONString(500);
        blogService.updateStatus(id,2);
        likeService.deleteLikePosts(id);

        return CommunityUtil.getJSONString(0);
    }

}
