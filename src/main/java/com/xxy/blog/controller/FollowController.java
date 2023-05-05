package com.xxy.blog.controller;

import com.xxy.blog.util.CommunityConstant;
import com.xxy.blog.util.CommunityUtil;
import com.xxy.blog.entity.Page;
import com.xxy.blog.entity.User;
//import com.nowcoder.community.event.EventProducer;
import com.xxy.blog.service.IFollowService;
import com.xxy.blog.service.IUserService;
import com.xxy.blog.util.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private IFollowService followService;

    @Autowired
    private LoginUser loginUser;

    @Autowired
    private IUserService userService;

    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = loginUser.getUser();
        followService.follow(user.getId(), entityType,entityId);
        return CommunityUtil.getJSONString(0,"已关注!");
    }

    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = loginUser.getUser();

        followService.unfollow(user.getId(), entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注!");
    }

    @RequestMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }

        model.addAttribute("user",user);
        page.setLimit(6);
        page.setPath("/followees/"+userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        if(userList != null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }

        model.addAttribute("users",userList);
        return "/page/followee";
    }

    @RequestMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }

        model.addAttribute("user",user);

        page.setLimit(6);
        page.setPath("/followers/"+userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if(userList != null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }

        model.addAttribute("users",userList);
        return "/page/follower";
    }

    private boolean hasFollowed(int userId){
        if(loginUser.getUser() == null){
            return false;
        }
        return followService.hasFollowed(loginUser.getUser().getId(),ENTITY_TYPE_USER,userId);
    }

}




