package com.example.tagmate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tagmate.common.ErrorCode;
import com.example.tagmate.common.UserConstant;
import com.example.tagmate.exception.BusinessException;
import com.example.tagmate.model.domain.User;
import com.example.tagmate.model.request.UserLoginRequest;
import com.example.tagmate.model.request.UserRegisterRequest;
import com.example.tagmate.model.request.UserUpdateRequest;
import com.example.tagmate.service.UserService;
import com.example.tagmate.mapper.UserMapper;
import com.example.tagmate.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.tagmate.common.UserConstant.USER_LOGIN_STATE;

/**
 * @author 86150
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-11-10 17:03:32
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     * todo: 考虑更换盐值的内容
     */
    private static final String SALT = "sunsan";

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户信息（脱敏后）
     */
    @Override
    public User userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String username = userRegisterRequest.getUsername();
        String planetCode = userRegisterRequest.getPlanetCode();
        String email = userRegisterRequest.getEmail();
        String phone = userRegisterRequest.getPhone();
        Integer gender = userRegisterRequest.getGender();

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 账号长度校验
        if (userAccount.length() < 4 || userAccount.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度错误");
        }

        // 密码长度校验
        if (userPassword.length() < 6 || userPassword.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度错误");
        }

        // 校验密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 账号不能包含特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }

        // 星球编号不能重复
        if (StringUtils.isNotBlank(planetCode)) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("planetCode", planetCode);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号重复");
            }
        }

        // 账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 插入数据
        User user = new User();
        BeanUtils.copyProperties(userRegisterRequest, user);
        user.setUserPassword(encryptPassword);
        user.setUserStatus(UserConstant.USER_STATUS_NORMAL);
        user.setUserRole(UserConstant.DEFAULT_ROLE);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);

        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        return getSafetyUser(user);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @return 登录用户信息（脱敏后）
     */
    @Override
    public User userLogin(UserLoginRequest userLoginRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 账号长度校验
        if (userAccount.length() < 4 || userAccount.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度错误");
        }

        // 密码长度校验
        if (userPassword.length() < 6 || userPassword.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }

        // 4. 校验用户状态
        if (user.getUserStatus() != UserConstant.USER_STATUS_NORMAL) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户状态异常");
        }

        // 5. 更新用户登录时间
        user.setUpdateTime(new Date());
        this.updateById(user);

        return getSafetyUser(user);
    }

    /**
     * 用户脱敏
     *
     * @param originUser 原始用户对象
     * @return 脱敏后的用户对象
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }

        User safetyUser = new User();
        // 基本信息（安全）
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        // 个人信息展示（安全）
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        // 系统状态信息（安全）
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        // 时间信息(安全)
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUpdateTime(originUser.getUpdateTime());
        // 不返回密码等敏感信息
        return safetyUser;
    }

    /**
     * 用户注销
     */
    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    /**
     * 根据用户标签搜索用户(内存过滤)
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 在内存中查询是否存在所需要的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            // 判断容器是否为空，如果为空返回参数里面的默认值new Hash
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 更新用户信息
     *
     * @param request   用户更新请求
     * @param loginUser 当前登录用户
     * @return 更新后的用户信息（脱敏后）
     */
    @Override
    public User updateUser(UserUpdateRequest request, User loginUser) {
        long userId = request.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }

        // 如果是管理员，允许更新任意用户
        // 非管理员，只允许更新当前用户
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限修改其他用户信息");
        }

        // 查询用户是否存在
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        // todo 需要理解的内容
        // 创建更新对象并复制请求中的属性
        User updateUser = new User();
        BeanUtils.copyProperties(request, updateUser);
        updateUser.setId(userId);
        updateUser.setUpdateTime(new Date());

        // 执行更新操作
        int result = userMapper.updateById(updateUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }

        // 返回更新后的用户信息（脱敏）
        User updatedUser = userMapper.selectById(userId);
        return getSafetyUser(updatedUser);
    }

    /**
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     * 
     * @param loginUser
     *                  return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 这个方法特别重要
     * 匹配用户
     * 
     * @param num
     * @param loginUser
     * @return
     */

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        // 如果标签不为空
        queryWrapper.isNotNull("tags");
        List<User> userList = userMapper.selectList(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());

        List<Pair<User, Long>> list = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或为当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            // 用户和对应的距离添加到列表之中，方便列表排序
            list.add(Pair.of(user, distance));

        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) a.getValue().compareTo(b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的userId列表
        List<Long> topUserIdList = topUserPairList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        // 创建查询条件
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", topUserIdList);

        // User1,User2,User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : topUserIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * todo
     * 根据标签搜索用户(SQL查询)
     * 
     * @param tagNameList 用户拥有的标签
     * @return
     */
    // 此方法暂时不使用
    @Deprecated
    public List<User> searchUsersByTagsBySQL(List<String> tagNameList) {

        return Collections.emptyList();
    }

}
