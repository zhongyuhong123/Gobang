package org.example.gobang.api;

import org.example.gobang.model.User;
import org.example.gobang.model.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class UserAPI {

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
        //这里true含义：如果为true，会话存在直接返回，不存在则创建一个
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        return new ApiResponse(true, "登录成功", user);
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
            HttpSession httpSession = req.getSession(false);
            User user = (User) httpSession.getAttribute("user");
            //拿着这个user对象，去数据库中找，找到最新的数据
            User newUser = userMapper.selectByName(user.getUsername());
            return new ApiResponse(true, "获取用户信息成功", newUser);
        }catch (NullPointerException e){
            return new ApiResponse(false, "用户未登录", null);
        }
    }
}