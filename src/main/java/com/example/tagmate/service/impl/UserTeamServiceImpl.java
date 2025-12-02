package com.example.tagmate.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tagmate.model.domain.UserTeam;
import com.example.tagmate.service.UserTeamService;
import com.example.tagmate.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 86150
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2025-11-21 20:26:21
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




