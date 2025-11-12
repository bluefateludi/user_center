package com.example.usercenter.controller;

import com.example.usercenter.common.BaseResponse;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.common.ResultUtils;
import com.example.usercenter.exception.BusinessException;
import com.example.usercenter.model.domain.dto.UserLoginRequest;
import com.example.usercenter.model.domain.dto.UserRegisterRequest;
import com.example.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 用户控制器
 * 
 * @author 小鹿
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户ID
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        // 校验密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result, "注册成功");
    }
    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @return 登录用户ID
     */
    @PostMapping("/login")
    public BaseResponse<Long> userLogin(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        long result = userService.userLogin(userLoginRequest);
        return ResultUtils.success(result, "登录成功");
    }
}