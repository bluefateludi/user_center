package com.example.tagmate.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 队伍加入请求体
 * 
 * @TableName team
 */

@Data
public class TeamQuitRequest implements Serializable {


    private static final long serialVersionUID = 1L;
    /**
     * id
     */

    private Long teamId;
}