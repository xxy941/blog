package com.xxy.blog.service.impl;

import com.xxy.blog.dao.UserMapper;
import com.xxy.blog.util.CommunityConstant;
import com.xxy.blog.util.CommunityUtil;
import com.xxy.blog.entity.LoginTicket;
import com.xxy.blog.entity.User;
import com.xxy.blog.service.IUserService;
import com.xxy.blog.util.MailClient;
import com.xxy.blog.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements IUserService, CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    @Override
    public User findUserInfoById(int id) {
        User user = userMapper.selectUserInfoById(id);
        return user;
    }

    @Override
    public Map<String, Object> register(User user,String confirmPassword) {
        Map<String,Object> map = new HashMap<>();
        /** 对空值进行处理*/
        if(user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
        if(user.getUsername().length()>15){
            map.put("usernameMsg","账号长度不能大于16!");
            return map;
        }
        if(user.getPassword().length()<6||user.getPassword().length()>15){
            map.put("passwordMsg","密码长度应大于6小于16!");
            return map;
        }

        if(!user.getPassword().equals(confirmPassword)){
            map.put("passwordMsg","密码不一致!");
            return map;
        }
        /** 验证账号 */
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","账号已存在!");
            return map;
        }

        /** 注册用户 */
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID().substring(0,4));
        user.setHeaderUrl("/img/default.jpg");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        /** 激活邮件 */
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        context.setVariable("activation",user.getActivationCode());
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(), "账户激活码" ,content);
        return map;
    }

    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        /** 空值处理 */
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }

        /** 验证账号 */
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","该账号不存在!");
            return map;
        }
        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活!");
            return map;
        }

        /** 验证密码 */
        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确!");
            return map;
        }

        /** 生成登录凭证 */
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);



        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    @Override
    public void logout(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    @Override
    public LoginTicket findLoginTicket(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    @Override
    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId,headerUrl);
        return rows;
    }

    @Override
    public int updatePassword(int userId, String newpassword) {
        int rows = userMapper.updatePassword(userId,newpassword);
        return rows;
    }

    @Override
    public int updateUserInfo(int id, int age, String sex, String education, String introduction) {
        int rows = userMapper.updateUserInfo(id,age,sex,education,introduction);
        return rows;
    }

    @Override
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }


}
