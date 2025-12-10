package com.yangshengzhou.gobang.mapper;

import com.yangshengzhou.gobang.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    // 往数据库里面插入一个用户，用户注册功能
    void insert(User user);

    // 根据用户名，来查询用户的详细信息，用于登录功能
    User selectByName(String username);

    // 根据用户ID查询用户信息
    User selectById(@Param("userId") int userId);

    // 更新用户信息
    void update(User user);

    // 总比赛场次+1，获胜场次+1，分数 +30
    void userWin(int userId);

    // 总比赛场次+1，分数 -30
    void userLose(int userId);
    
    // 平局：总比赛场次+1，分数 +10
    void userDraw(int userId);
}