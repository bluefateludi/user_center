package com.example.tagmate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tagmate.model.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 86150
 * @description 针对表【user(用户)】的数据库操作Mapper
 * @createDate 2025-11-10 17:03:32
 * @Entity com.example.usercenter.model.domain.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
