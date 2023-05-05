package com.xxy.blog.controller;

import com.google.code.kaptcha.Producer;
import com.xxy.blog.util.CommunityConstant;
import com.xxy.blog.util.CommunityUtil;
import com.xxy.blog.entity.User;
import com.xxy.blog.service.IUserService;
import com.xxy.blog.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private IUserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/register")
    public String getRegisterPage(){
        return "/page/register";
    }

    @RequestMapping("/login")
    public String getLoginPage(){
        return "/page/login";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user, String password2){
        Map<String,Object> map = userService.register(user,password2);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功,我们已经向你的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target","/index");
            model.addAttribute("userId",user.getId());
            return "/page/activation";
        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/page/register";
        }
    }

    @RequestMapping(path = "/activation",method = RequestMethod.POST)
    public String activation(Model model,Integer userId,String activation){
        int result = userService.activation(userId,activation);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target","/login");
        }else if(result ==  ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作,该账号已经激活过了!");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("activationMsg","激活失败,您提供的激活码不正确!");
            model.addAttribute("userId",userId);
            return "/page/activation";
        }
        return "/page/login";
    }

    @RequestMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        /** 生成验证码 */
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        /** 验证码的归属者 */
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(300);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        /** 将验证码存入Redis */
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        /** 将图片输出给浏览器 */
        response.setContentType("/image/png");
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code, Model model,HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kapchaOwner){
        /** 检查验证码 */
        String kaptcha = null;
        if (kapchaOwner == null){
            model.addAttribute("codeMsg","验证码过期!");
            return "/page/login";
        }
        if(StringUtils.isNotBlank(kapchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kapchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确!");
            return "/page/login";
        }
        /** 检查账号，密码 */
        int expiredSeconds = 43200;
        Map<String,Object> map = userService.login(username,password,expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/page/login";
        }
    }

    @RequestMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/index";
    }

}
