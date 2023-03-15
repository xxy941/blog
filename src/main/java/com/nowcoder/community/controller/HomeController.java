package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Blog;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.IBlogService;
import com.nowcoder.community.service.ILikeService;
import com.nowcoder.community.service.IUserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

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

        List<Blog> list = blogService.findBlogs(0,page.getOffset(),page.getLimit(),orderMode);
        List<Map<String,Object>> blogs = new ArrayList<>();
        if(list != null){
            for (Blog blog : list) {
                Map<String,Object> map = new HashMap<>();
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_BLOG, blog.getId());
                User user = userService.findUserById(blog.getUserId());
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
        return "/site/searchpage";
    }

    @RequestMapping("/manager")
    public String getManagerPage(Model model, Page page){
        /** 方法调用前，SpringMVC会自动实例化Model和Page,并且将Page注入到Model */
        /** 所以在thymeleaf中可以直接访问Page对象中的数据 */
        page.setRows(blogService.findBlogRows(0));
        page.setPath("/manager");

        List<Blog> list = blogService.findBlogs(0,page.getOffset(),page.getLimit(),0);
        List<Map<String,Object>> blogs = new ArrayList<>();
        if(list != null){
            for (Blog blog : list) {
                Map<String,Object> map = new HashMap<>();
                User user = userService.findUserById(blog.getUserId());
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_BLOG, blog.getId());
                map.put("blog", blog);
                map.put("user",user);
                map.put("likeCount",likeCount);
                blogs.add(map);
            }
        }
        model.addAttribute("blogs",blogs);
        return "/site/manager";
    }

    @RequestMapping("/classify")
    public String getClassifyPage(){
        return "/site/classify";
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
