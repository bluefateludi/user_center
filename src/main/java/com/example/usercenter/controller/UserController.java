package com.example.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.usercenter.common.BaseResponse;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.common.ResultUtils;
import com.example.usercenter.exception.BusinessException;
import com.example.usercenter.model.domain.User;
import com.example.usercenter.model.domain.dto.UserLoginRequest;
import com.example.usercenter.model.domain.dto.UserRegisterRequest;
import com.example.usercenter.model.domain.dto.UserUpdateRequest;
import com.example.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.usercenter.common.UserConstant.ADMIN_ROLE;
import static com.example.usercenter.common.UserConstant.USER_LOGIN_STATE;

/**
 * 用户控制器
 *
 * @author 小鹿
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等相关接口")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户ID
     */
    @Operation(summary = "用户注册", description = "新用户注册接口，创建用户账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(
            @Parameter(description = "用户注册信息", required = true)
            @Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        User result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result.getId(), "注册成功");
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @return 登录用户ID
     */
    @Operation(summary = "用户登录", description = "用户账号登录接口")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "账号或密码错误")
    })
    @PostMapping("/login")
    public BaseResponse<Long> userLogin(
            @Parameter(description = "用户登录信息", required = true)
            @Valid @RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        User result = userService.userLogin(userLoginRequest);
        // 将用户登录信息存储到Session中
        request.getSession().setAttribute(USER_LOGIN_STATE, result);
        return ResultUtils.success(result.getId(), "登录成功");
    }

    /**
     * 用户注销
     */
    @Operation(summary = "用户注销", description = "用户退出登录接口",
            security = {@SecurityRequirement(name = "session-auth")})
    @ApiResponse(responseCode = "200", description = "注销成功")
    @PostMapping("/logout")
    public BaseResponse<Long> userLogout(HttpServletRequest request) {
        userService.userLogout(request);
        return ResultUtils.success(0L, "注销成功");
    }

    /**
     * 获取当前用户
     */
    @Operation(summary = "获取当前用户", description = "获取当前登录用户的信息",
            security = {@SecurityRequirement(name = "session-auth")})
    @ApiResponse(responseCode = "200", description = "获取成功",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/current")
    public BaseResponse<User> getCurrentUser(
            @Parameter(description = "HTTP请求对象，包含用户会话信息")
            HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        user = userService.getSafetyUser(user);
        return ResultUtils.success(user, "获取当前用户成功");

    }

    /**
     * 根据ID查找用户
     */
    @Operation(summary = "根据ID查找用户", description = "根据用户ID查找用户信息（仅管理员）",
            security = {@SecurityRequirement(name = "session-auth")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查找成功",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "无权限访问"),
            @ApiResponse(responseCode = "400", description = "参数错误或用户不存在")
    })
    @GetMapping("/search")
    public BaseResponse<User> searchUser(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "HTTP请求对象，用于权限验证") HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID参数错误");
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser, "查找用户成功");
    }

    /**
     * 是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        //仅管理员可以查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user =(User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户", description = "更新用户信息，管理员可更新任意用户",
            security = {@SecurityRequirement(name = "session-auth")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "无权限访问")
    })
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(
            @Parameter(description = "用户更新请求") @RequestBody UserUpdateRequest userUpdateRequest,
            @Parameter(description = "HTTP请求对象，用于权限验证") HttpServletRequest request) {
        if(userUpdateRequest== null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User )request.getSession().getAttribute(USER_LOGIN_STATE);
        if(loginUser ==null) {
        throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Long userId =userUpdateRequest.getId();
        //如果是管理员，允许更新任意用户
        //如果不是管理员，只允许更新当前用户
        if(!isAdmin(request) && !userId.equals(loginUser.getId())){
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        User user = new User();
        user.setId(userId);
        user.setUsername(userUpdateRequest.getUsername());
        user.setUserAccount(userUpdateRequest.getUserAccount());
        user.setAvatarUrl(userUpdateRequest.getAvatarUrl());
        user.setGender(userUpdateRequest.getGender());
        user.setPhone(userUpdateRequest.getPhone());
        user.setEmail(userUpdateRequest.getEmail());
        user.setUserStatus(userUpdateRequest.getUserStatus());
        user.setPlanetCode(userUpdateRequest.getPlanetCode());
        //仅管理员可以修改用户角色
        if(isAdmin(request)){
            user.setUserRole(userUpdateRequest.getUserRole());
        }
        boolean result =userService.updateById(user);
        return ResultUtils.success(result, "更新用户成功");
    }

    /**
     * 删除用户
     * 只有管理员才能删除用户
     */
    @Operation(summary = "删除用户", description = "根据用户ID删除用户（仅管理员）",
            security = {@SecurityRequirement(name = "session-auth")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或用户不存在"),
            @ApiResponse(responseCode = "401", description = "无权限访问")
    })
    @DeleteMapping("/{userId}")
    public BaseResponse<Boolean> deleteUser(
            @Parameter(description = "要删除的用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "HTTP请求对象，用于权限验证") HttpServletRequest request) {
        //判断是否是管理员
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        //id出现错误
        if(userId == null || userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID参数错误");
        }
        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        boolean result = userService.removeById(userId);
        return ResultUtils.success(result, "删除用户成功");
    }

}