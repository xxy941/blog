package com.xxy.blog.controller;

import com.xxy.blog.entity.User;
import com.xxy.blog.service.IBlogService;
import com.xxy.blog.service.IFollowService;
import com.xxy.blog.service.ILikeService;
import com.xxy.blog.service.IUserService;
import com.xxy.blog.util.CommunityConstant;
import com.xxy.blog.util.CommunityUtil;
import com.xxy.blog.util.LoginUser;
import com.xxy.blog.entity.Blog;
import com.xxy.blog.entity.Page;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private IUserService userService;

    @Autowired
    private LoginUser loginUser;

    @Autowired
    private ILikeService likeService;

    @Autowired
    private IFollowService followService;

    @Autowired
    private IBlogService blogService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @RequestMapping("/mypost")
    public String getMyDiscussPostPage(Model model, Page page){
        User user = loginUser.getUser();
        page.setPath("/user/mypost");
        page.setRows(blogService.findBlogRows(user.getId()));
        page.setLimit(5);
        List<Blog> list = blogService.findBlogs(user.getId(), page.getOffset(), page.getLimit(),0);
        List<Map<String,Object>> myDiscussPosts = new ArrayList<>();
        if(list != null){
            for (Blog post : list) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                map.put("user",user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_BLOG,post.getId());
                map.put("likeCount",likeCount);
                myDiscussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",myDiscussPosts);
        return "/page/my-post";
    }

    @RequestMapping("/avatar")
    public String getAvatarPage(Model model){
        /** 上传文件名称 */
        String fileName = CommunityUtil.generateUUID();
        /** 设置响应信息 */
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        /** 生成上传凭证 */
        Auth auth = Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);

        return "/page/avatar";
    }

    @RequestMapping("/password")
    public String getPasswordPage(Model model){
        return "/page/password";
    }

    /** 更新头像路径 */
    @RequestMapping(path = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(loginUser.getUser().getId(),url);

        return CommunityUtil.getJSONString(0);
    }


    @RequestMapping(path = "/change_password",method = RequestMethod.POST)
    public String changePassword(@Param("password") String password, @Param("newPassword") String newPassword, @Param("newPassword2") String newPassword2,Model model){
        if(StringUtils.isBlank(password)){
            model.addAttribute("passwordError","原密码不能为空!");
            return "/page/password";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordError","新密码不能为空!");
            return "/page/password";
        }

        if(newPassword.length()<6||newPassword.length()>16){
            model.addAttribute("newPasswordError","密码长度应大于6小于16!");
            return "/page/password";
        }

        if(!newPassword.equals(newPassword2)){
            model.addAttribute("newPasswordError","新密码不一致!");
            return "/page/password";
        }

        User user = loginUser.getUser();
        password = CommunityUtil.md5(password + user.getSalt());
        if(password.equals(user.getPassword())){
            newPassword = CommunityUtil.md5(newPassword + user.getSalt());
            userService.updatePassword(user.getId(),newPassword);
            return "redirect:/index";
        }else {
            model.addAttribute("passwordError","原密码错误!");
            return "/page/password";
        }
    }

    /** 个人主页加个人帖子 */
    @RequestMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId,Model model,Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        int likeCount = likeService.findUserLikeCount(userId);
        /** 用户信息 */
        model.addAttribute("user",user);
        /** 点赞数量 */
        model.addAttribute("likeCount",likeCount);
        /** 关注数量 */
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        /** 粉丝数量 */
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        /** 是否已关注 */
        boolean hasFollowed = false;
        if(loginUser.getUser() != null){
            hasFollowed = followService.hasFollowed(loginUser.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        /** 我的帖子 */
        page.setPath("/user/profile/" + userId);
        page.setRows(blogService.findBlogRows(userId));
        page.setLimit(5);
        List<Blog> list = blogService.findBlogs(user.getId(), page.getOffset(), page.getLimit(),0);
        List<Map<String,Object>> myBlogs = new ArrayList<>();
        if(list != null){
            for (Blog blog : list) {
                Map<String,Object> map = new HashMap<>();
                if(blog.getContent().length()>50){
                    blog.setContent(blog.getContent().substring(0,50) + "...");
                }
                map.put("blog",blog);
                map.put("user",user);
                long userLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_BLOG,blog.getId());
                map.put("userLikeCount",userLikeCount);
                myBlogs.add(map);
            }
        }
        model.addAttribute("blogs",myBlogs);

        return "/page/profile";
    }

    /** 个人主页加点赞帖子 */
    @RequestMapping("/likepost/{userId}")
    public String getLikePage(@PathVariable("userId") int userId,Model model,Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        int likeCount = likeService.findUserLikeCount(userId);
        /** 用户信息 */
        model.addAttribute("user",user);
        /** 点赞数量 */
        model.addAttribute("likeCount",likeCount);
        /** 关注数量 */
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        /** 粉丝数量 */
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        /** 是否已关注 */
        boolean hasFollowed = false;
        if(loginUser.getUser() != null){
            hasFollowed = followService.hasFollowed(loginUser.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        /** 点赞的帖子 */
        page.setPath("/user/likepost/" + userId);
        page.setRows((int) likeService.findLikePostCount(userId));
        page.setLimit(5);
        List<Blog> list = likeService.findLikePosts(userId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> myBlogs = new ArrayList<>();
        if(list != null){
            for (Blog blog : list) {
                Map<String,Object> map = new HashMap<>();
                if(blog.getContent().length()>50){
                    blog.setContent(blog.getContent().substring(0,50) + "...");
                }
                map.put("blog",blog);
                map.put("user",userService.findUserById(blog.getUserId()));
                long userLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_BLOG,blog.getId());
                map.put("userLikeCount",userLikeCount);
                myBlogs.add(map);
            }
        }
        model.addAttribute("blogs",myBlogs);

        return "/page/profile";
    }

    /** 个人主页加个人帖子 */
    @RequestMapping("/userinfo/{userId}")
    public String getUserInfoPage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserInfoById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }

        model.addAttribute("userinfo",user);

        return "/page/myuserinfo";
    }

    @RequestMapping("/editinfo")
    public String getEditUserInfoPage(Model model){
        User user = userService.findUserInfoById(loginUser.getUser().getId());
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("userinfo",user);

        return "/page/edituserinfo";
    }

    @RequestMapping("/edituserinfo")
    public String editUserInfo(int age,String sex,String education,String introduction ,Model model){
        User user = loginUser.getUser();
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        userService.updateUserInfo(user.getId(),age,sex,education,introduction);
        user.setAge(age);
        user.setSex(sex);
        user.setEducation(education);
        user.setIntroduction(introduction);
        model.addAttribute("userinfo",user);

        return "/page/edituserinfo";
    }


}
