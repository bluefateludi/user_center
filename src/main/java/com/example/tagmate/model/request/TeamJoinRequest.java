package com.example.tagmate.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 队伍加入请求体
 * 
 * @TableName team
 */

@Data
public class TeamJoinRequest implements Serializable {

    /**
     * id
     */

    private Long teamId;
    /**
     * 密码
     */
    private String password;
}