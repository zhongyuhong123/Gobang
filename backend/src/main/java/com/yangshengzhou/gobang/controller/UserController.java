package com.yangshengzhou.gobang.controller;

import com.yangshengzhou.gobang.entity.User;
import com.yangshengzhou.gobang.mapper.UserMapper;
import com.yangshengzhou.gobang.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    // 内部类，用于统一API响应格式
    private static class ApiResponse {
        private boolean status;
        private String message;
        private Object data;

        public ApiResponse(boolean status, String message, Object data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

    @Resource
    public UserMapper userMapper;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    @ResponseBody
    public Object login(String username, String password, HttpServletRequest req) {
        //根据username去数据库中查找，如果能查到并密码一致则成功登入
        User user = userMapper.selectByName(username);
        System.out.println("[login] user="+user);
        if(user==null || !passwordEncoder.matches(password, user.getPassword())){
            System.out.println("登入失败!");
            return new ApiResponse(false, "用户名或密码错误", null);
        }
        
        // 生成双token
        String accessToken = jwtTokenUtil.generateAccessToken(String.valueOf(user.getUserId()), user.getUsername());
        String refreshToken = jwtTokenUtil.generateRefreshToken(String.valueOf(user.getUserId()), user.getUsername());
        
        // 创建登录响应数据
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("user", user);
        loginData.put("accessToken", accessToken);
        loginData.put("refreshToken", refreshToken);
        loginData.put("tokenType", "Bearer");
        loginData.put("expiresIn", 86400); // 24小时，单位秒
        
        return new ApiResponse(true, "登录成功", loginData);
    }

    @PostMapping("/register")
    @ResponseBody
    public Object register(String username, String password) {
        try{
            User user = new User();
            user.setUsername(username);
            // 对密码进行加密
            user.setPassword(passwordEncoder.encode(password));
            userMapper.insert(user);
            return new ApiResponse(true, "注册成功", user);
        }catch (org.springframework.dao.DuplicateKeyException e){
            return new ApiResponse(false, "用户名已存在", null);
        }
    }

    @GetMapping("/userInfo")
    @ResponseBody
    public Object getUserInfo(HttpServletRequest req) {
        try{
            // 从请求头中获取token并验证
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtTokenUtil.validateAccessToken(token)) {
                    String username = jwtTokenUtil.getUsernameFromToken(token);
                    User user = userMapper.selectByName(username);
                    return new ApiResponse(true, "获取用户信息成功", user);
                }
            }
            return new ApiResponse(false, "token无效或已过期", null);
        }catch (Exception e){
            return new ApiResponse(false, "获取用户信息失败", null);
        }
    }
}