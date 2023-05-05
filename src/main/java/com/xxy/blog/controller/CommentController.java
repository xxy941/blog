package com.xxy.blog.controller;

import com.xxy.blog.util.CommunityConstant;
import com.xxy.blog.util.LoginUser;
import com.xxy.blog.entity.Comment;
//import com.nowcoder.community.event.EventProducer;
import com.xxy.blog.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private ICommentService commentService;

    @Autowired
    private LoginUser loginUser;

    @RequestMapping(path = "/add/{blogId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("blogId") int blogId, Comment comment){
        comment.setUserId(loginUser.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        return "redirect:/blog/detail/" + blogId;
    }
}
