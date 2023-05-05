package com.xxy.blog.service;

import com.xxy.blog.entity.LoginTicket;
import com.xxy.blog.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public interface IUserService {

    public User findUserById(int id);

    public User findUserInfoById(int id);

    public Map<String,Object> register(User user,String confirmPassword);

    public int activation(int userId,String code);

    public Map<String,Object> login(String username,String password,long expiredSeconds);

    public void logout(String ticket);

    public LoginTicket findLoginTicket(String ticket);

    public int updateHeader(int userId,String headerUrl);

    public int updatePassword(int userId,String newpassword);

    public int updateUserInfo(int id,int age,String sex,String education,String introduction);

    public User findUserByName(String username);

    public Collection<? extends GrantedAuthority> getAuthorities(int userId);

}
