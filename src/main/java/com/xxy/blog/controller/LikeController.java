package com.xxy.blog.controller;

import com.xxy.blog.entity.User;
import com.xxy.blog.service.ILikeService;
import com.xxy.blog.util.CommunityConstant;
import com.xxy.blog.util.CommunityUtil;
import com.xxy.blog.util.LoginUser;
//import com.nowcoder.community.event.EventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private ILikeService likeService;

    @Autowired
    private LoginUser loginUser;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId){
        User user = loginUser.getUser();
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        long likeCount = likeService.findEntityLikeCount(entityType,entityId);
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);
        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);
        return CommunityUtil.getJSONString(0,null,map);
    }

}
