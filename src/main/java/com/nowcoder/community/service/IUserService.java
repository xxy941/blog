package com.nowcoder.community.service;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;

import java.util.Map;

public interface IUserService {
    public User findUserById(int id);

    public Map<String,Object> register(User user);

    public int activation(int userId,String code);

    public Map<String,Object> login(String username,String password,long expiredSeconds);

    public void logout(String ticket);

    public LoginTicket findLoginTicket(String ticket);

    public int updateHeader(int userId,String headerUrl);

    public int updatePassword(int userId,String newpassword);
}
