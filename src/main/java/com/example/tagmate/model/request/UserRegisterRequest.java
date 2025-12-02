package com.example.tagmate.model.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户注册请求DTO
 * @author 86150
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户账号
     */
        @NotBlank(message = "用户账号不能为空")
    @Size(min = 4, max = 32, message = "用户账号长度必须在4-32个字符之间")
    private String userAccount;

    /**
     * 用户密码
     */
    @NotBlank(message = "用户密码不能为空")
    @Size(min = 6, max = 32, message = "用户密码长度必须在6-32个字符之间")
    private String userPassword;

    /**
     * 校验密码
     */
    @NotBlank(message = "校验密码不能为空")
    private String checkPassword;

    /**
     * 用户昵称
     */
    @Size(max = 64, message = "用户昵称长度不能超过64个字符")
    private String username;

    /**
     * 星球编号
     */
    @Size(max = 32, message = "星球编号长度不能超过32个字符")
    private String planetCode;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 电话
     */
    @Size(max = 20, message = "电话号码长度不能超过20个字符")
    private String phone;

    /**
     * 性别
     */
    @Min(value = 0, message = "性别值不合法")
    private Integer gender;
}