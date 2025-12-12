package com.yangshengzhou.gobang.mapper;

import com.yangshengzhou.gobang.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    void insert(User user);

    User selectByName(String username);

    User selectById(@Param("userId") int userId);

    void update(User user);

    void userWin(int userId);

    void userLose(int userId);
    
    void userDraw(int userId);
}