package com.example.usercenter.service;

import com.example.usercenter.model.domain.User;
import com.example.usercenter.model.domain.dto.UserLoginRequest;
import com.example.usercenter.model.domain.dto.UserRegisterRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 86150
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-11-10 17:03:32
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户ID
     */
    User userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     * 
     * @param userLoginRequest
     * @return
     */
    User userLogin(UserLoginRequest userLoginRequest);

    /**
     * 用户脱敏
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     */
    void userLogout(HttpServletRequest request);

}
