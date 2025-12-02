package com.example.tagmate.service;

import com.example.tagmate.model.domain.Team;
import com.example.tagmate.model.domain.User;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tagmate.model.dto.TeamQuery;
import com.example.tagmate.model.request.TeamJoinRequest;
import com.example.tagmate.model.request.TeamQuitRequest;
import com.example.tagmate.model.request.TeamUpdateRequest;
import com.example.tagmate.model.vo.TeamUserVO;

import java.util.List;

/**
* @author 86150
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-11-21 12:40:39
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     *
     * @param team
     * @return
     */
    long addTeam(Team team, User loginUser);
    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);
    /**
     * 更新队伍
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);
    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);
/**
     * 删除队伍
     *
     * @param id
     * @param loginUser
     * @return
     */
boolean deleteTeam(long id, User loginUser);

}
