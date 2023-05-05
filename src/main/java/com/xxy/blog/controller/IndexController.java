package com.xxy.blog.controller;

import com.xxy.blog.service.IBlogService;
import com.xxy.blog.service.ILikeService;
import com.xxy.blog.util.CommunityConstant;
import com.xxy.blog.entity.Blog;
import com.xxy.blog.entity.Page;
import com.xxy.blog.entity.User;
import com.xxy.blog.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController implements CommunityConstant {

    @Autowired
    private IBlogService blogService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ILikeService likeService;

    @RequestMapping(path = "/" )
    public String root(){
        return "forward:/index";
    }

    @RequestMapping("/index")
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode",defaultValue = "0") int orderMode){
        /** 方法调用前，SpringMVC会自动实例化Model和Page,并且将Page注入到Model */
        /** 所以在thymeleaf中可以直接访问Page对象中的数据 */
        page.setRows(blogService.findBlogRows(0));
        page.setPath("/index?orderMode=" + orderMode);
        page.setLimit(6);

        List<Blog> list = blogService.findBlogs(0,page.getOffset(),page.getLimit(),orderMode);
        List<Map<String,Object>> blogs = new ArrayList<>();
        if(list != null){
            for (Blog blog : list) {
                Map<String,Object> map = new HashMap<>();
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_BLOG, blog.getId());
                User user = userService.findUserById(blog.getUserId());
                if(blog.getContent().length()>50){
                    blog.setContent(blog.getContent().substring(0,50) + "...");
                }
                map.put("blog", blog);
                map.put("user",user);
                map.put("likeCount",likeCount);
                blogs.add(map);
            }
        }
        model.addAttribute("blogs",blogs);
        model.addAttribute("orderMode",orderMode);
        return "/index";
    }

    @RequestMapping("/searchpage")
    public String getSearchPage(){
        return "/page/searchpage";
    }

    @RequestMapping("/manager")
    public String getManagerPage(Model model, Page page){
        page.setRows(blogService.findBlogRows(0));
        page.setPath("/manager");
        page.setLimit(6);

        List<Blog> list = blogService.findBlogs(0,page.getOffset(),page.getLimit(),0);
        List<Map<String,Object>> blogs = new ArrayList<>();
        if(list != null){
            for (Blog blog : list) {
                Map<String,Object> map = new HashMap<>();
                User user = userService.findUserById(blog.getUserId());
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_BLOG, blog.getId());
                if(blog.getContent().length()>50){
                    blog.setContent(blog.getContent().substring(0,50) + "...");
                }
                map.put("blog", blog);
                map.put("user",user);
                map.put("likeCount",likeCount);
                blogs.add(map);
            }
        }
        model.addAttribute("blogs",blogs);
        return "/page/manager";
    }

    @RequestMapping("/classify")
    public String getClassifyPage(){
        return "/page/classify";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

    @RequestMapping("/denied")
    public String getDeniedPage(){
        return "/error/404";
    }
}
