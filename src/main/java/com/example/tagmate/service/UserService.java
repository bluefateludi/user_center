package com.example.tagmate.service;

import com.example.tagmate.model.domain.User;
import com.example.tagmate.model.request.UserLoginRequest;
import com.example.tagmate.model.request.UserRegisterRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tagmate.model.request.UserUpdateRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 标签列表
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     *
     * @param request
     * @return
     */
    User updateUser(UserUpdateRequest request, User loginUser);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);


    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);

/**
 * 根据标签搜索用户(Sql查询版)
 *
 * @param tagNameList
 * @return
 */
List<User> searchUsersByTagsBySQL(List<String> tagNameList);
}
