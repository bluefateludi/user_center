package com.example.tagmate.model.request;


import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserLoginRequest {

    @NotBlank(message = "用户账号不能为空")
    private String userAccount;

@NotBlank(message = "用户密码不能为空")
    private String userPassword;



}
