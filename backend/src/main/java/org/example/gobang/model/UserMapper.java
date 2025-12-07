package org.example.gobang.model;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // 往数据库里面插入一个用户，用户注册功能
    void insert(User user);

    // 根据用户名，来查询用户的详细信息，用于登录功能
    User selectByName(String username);

    // 总比赛场次+1，获胜场次+1，分数 +30
    void userWin(int userId);

    // 总比赛场次+1，分数 -30
    void userLose(int userId);
}
