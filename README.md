# 五子棋游戏项目 (Gobang Game)

## 项目简介

这是一个基于前后端分离架构的在线五子棋游戏项目，支持玩家注册登录、创建房间、随机匹配、实时对战、积分系统等核心功能。

## 技术栈

### 前端
- **框架**: Vue 3 + Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **UI组件**: Element Plus
- **网络请求**: Axios
- **WebSocket**: 原生WebSocket API

### 后端
- **框架**: Spring Boot 2.6.4
- **持久层**: MyBatis
- **数据库**: MySQL 8.0
- **实时通信**: WebSocket
- **构建工具**: Maven

### 开发工具
- **IDE**: IntelliJ IDEA / VS Code
- **版本控制**: Git

## 项目结构

```
gobang/
├── backend/                  # 后端代码
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── org/example/gobang/
│   │   │   │       ├── GobangApplication.java  # 启动类
│   │   │   │       ├── api/                     # 接口层
│   │   │   │       ├── config/                  # 配置类
│   │   │   │       ├── game/                    # 游戏逻辑层
│   │   │   │       └── model/                   # 数据模型层
│   │   │   ├── resources/
│   │   │   │   ├── application.yml              # 配置文件
│   │   │   │   └── mapper/                      # MyBatis映射文件
│   │   └── test/                                # 测试代码
│   └── pom.xml                                  # Maven配置文件
├── frontend/                 # 前端代码
│   ├── src/
│   │   ├── assets/          # 静态资源
│   │   ├── components/      # Vue组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia状态管理
│   │   ├── views/           # 页面组件
│   │   └── main.js          # 入口文件
│   ├── index.html           # HTML模板
│   ├── package.json         # 依赖配置
│   └── vite.config.js       # Vite配置
├── LICENSE
└── README.md
```

## 功能模块

### 1. 用户系统
- 注册/登录
- 个人信息管理
- 头像上传

### 2. 游戏大厅
- 在线用户列表
- 房间列表
- 创建房间
- 加入房间

### 3. 匹配系统
- 随机匹配
- 好友邀请
- 段位匹配

### 4. 游戏核心
- 五子棋棋盘渲染
- 落子逻辑
- 胜负判断
- 悔棋功能
- 计时器

### 5. 房间系统
- 房间创建与管理
- 房间设置（棋盘大小、时间限制等）
- 观战功能

### 6. 积分系统
- 胜负积分计算
- 段位系统
- 排行榜

## 架构设计

### 1. 系统架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   前端客户端    │     │   后端服务器    │     │    数据库       │
│  (Vue 3 + Vite) │────▶│ (Spring Boot)   │────▶│   (MySQL)       │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        ▲                        ▲
        │ WebSocket               │
        └────────────────────────┘
           实时游戏通信
```

### 2. 核心流程

#### 用户登录流程
1. 用户输入用户名和密码
2. 前端发送请求到后端
3. 后端验证用户信息
4. 返回JWT令牌
5. 前端存储令牌并跳转到游戏大厅

#### 游戏匹配流程
1. 用户点击匹配按钮
2. 前端发送匹配请求
3. 后端匹配系统寻找对手
4. 匹配成功后创建房间
5. 通知双方进入游戏房间

#### 游戏对战流程
1. 玩家A落子
2. 前端发送落子请求
3. 后端验证落子合法性
4. 更新游戏状态
5. 广播游戏状态给双方
6. 判断胜负

## 快速开始

### 1. 环境要求
- JDK 1.8+
- MySQL 8.0+
- Node.js 16+
- npm 8+

### 2. 数据库配置

#### 创建数据库
```sql
CREATE DATABASE `java_gobang` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 导入数据
执行 `backend/src/main/db.sql` 文件中的SQL语句。

### 3. 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 4. 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 5. 访问项目
前端地址: `http://localhost:5173`
后端地址: `http://localhost:8080`

## API接口文档

### 用户接口
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/info` - 获取用户信息

### 游戏接口
- `POST /api/game/create` - 创建房间
- `GET /api/game/rooms` - 获取房间列表
- `POST /api/game/join` - 加入房间

### 匹配接口
- `POST /api/match/start` - 开始匹配
- `POST /api/match/cancel` - 取消匹配

### WebSocket接口
- 连接地址: `ws://localhost:8080/ws/game`
- 消息格式: JSON

## 开发规范

### 1. 代码规范
- 前端: ESLint + Prettier
- 后端: Alibaba Java Coding Guidelines

### 2. 分支管理
- `main`: 主分支，用于生产环境
- `develop`: 开发分支，用于集成测试
- `feature/*`: 功能分支，用于开发新功能
- `fix/*`: 修复分支，用于修复bug

### 3. 提交规范
遵循Conventional Commits规范，格式如下：
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

类型包括：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试代码
- `chore`: 构建工具或辅助工具的变动

## 部署说明

### 1. 开发环境
直接使用快速开始中的命令启动即可。

### 2. 生产环境

#### 后端部署
```bash
cd backend
mvn clean package -DskipTests
java -jar target/gobang-0.0.1-SNAPSHOT.jar
```

#### 前端部署
```bash
cd frontend
npm run build
# 将dist目录部署到Nginx或其他静态文件服务器
```

## 监控与维护

### 1. 日志
- 后端日志: `logs/gobang.log`
- 前端日志: 浏览器控制台

### 2. 常见问题
- 连接失败: 检查数据库配置和端口占用
- 匹配超时: 检查WebSocket连接和服务器负载
- 游戏卡顿: 检查网络延迟和服务器性能

## 项目成员

- 开发人员: [Your Name]
- 联系方式: [Your Email]

## 许可证

MIT License

## 更新日志

### v1.0.0 (2024-12-XX)
- 项目初始化
- 实现前后端分离架构
- 完成用户注册登录功能
- 实现游戏大厅和房间系统
- 完成五子棋核心游戏逻辑
