package com.example.tagmate.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tagmate.common.ErrorCode;
import com.example.tagmate.exception.BusinessException;
import com.example.tagmate.mapper.TeamMapper;
import com.example.tagmate.model.domain.Team;

import com.example.tagmate.model.domain.User;
import com.example.tagmate.service.TeamService;
import com.example.tagmate.service.UserService;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
* @author 86150
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2025-11-21 12:40:39
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {
@Resource
    private TeamMapper teamMapper;
@Resource
    private UserService userService;
@Resource
    private RedissonClient redissonClient;

@Override
@Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
    //校验
    if(team==null){
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }


}


}




