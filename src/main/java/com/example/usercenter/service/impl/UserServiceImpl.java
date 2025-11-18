package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.common.UserConstant;
import com.example.usercenter.exception.BusinessException;
import com.example.usercenter.model.domain.User;
import com.example.usercenter.model.domain.dto.UserLoginRequest;
import com.example.usercenter.model.domain.dto.UserRegisterRequest;
import com.example.usercenter.service.UserService;
import com.example.usercenter.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.usercenter.common.UserConstant.USER_LOGIN_STATE;

/**
 * @author 86150
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-11-10 17:03:32
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     * todo: 考虑更换盐值的内容
     */
    private static final String SALT = "sunsan";

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户信息（脱敏后）
     */
    @Override
    public User userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String username = userRegisterRequest.getUsername();
        String planetCode = userRegisterRequest.getPlanetCode();
        String email = userRegisterRequest.getEmail();
        String phone = userRegisterRequest.getPhone();
        Integer gender = userRegisterRequest.getGender();

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 账号长度校验
        if (userAccount.length() < 4 || userAccount.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度错误");
        }

        // 密码长度校验
        if (userPassword.length() < 6 || userPassword.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度错误");
        }

        // 校验密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 账号不能包含特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }

        // 星球编号不能重复
        if (StringUtils.isNotBlank(planetCode)) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("planetCode", planetCode);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号重复");
            }
        }

        // 账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 插入数据
        User user = new User();
        BeanUtils.copyProperties(userRegisterRequest, user);
        user.setUserPassword(encryptPassword);
        user.setUserStatus(UserConstant.USER_STATUS_NORMAL);
        user.setUserRole(UserConstant.DEFAULT_ROLE);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);

        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        return getSafetyUser(user);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @return 登录用户信息（脱敏后）
     */
    @Override
    public User userLogin(UserLoginRequest userLoginRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 账号长度校验
        if (userAccount.length() < 4 || userAccount.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度错误");
        }

        // 密码长度校验
        if (userPassword.length() < 6 || userPassword.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }

        // 4. 校验用户状态
        if (user.getUserStatus() != UserConstant.USER_STATUS_NORMAL) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户状态异常");
        }

        // 5. 更新用户登录时间
        user.setUpdateTime(new Date());
        this.updateById(user);

        return getSafetyUser(user);
    }


    /**
     * 用户脱敏
     *
     * @param originUser 原始用户对象
     * @return 脱敏后的用户对象
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }

        User safetyUser = new User();
        // 基本信息（安全）
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        // 个人信息展示（安全）
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        // 系统状态信息（安全）
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        // 时间信息(安全)
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUpdateTime(originUser.getUpdateTime());
        // 不返回密码等敏感信息
        return safetyUser;
    }

    /**
     * 用户注销
     */
    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }
}
