# 多棋类在线对战平台 (Multi-board Game Online Platform)

<div align="center">
  <a href="https://vuejs.org/" target="_blank">
    <img src="https://img.shields.io/badge/Vue.js-3.x-4FC08D.svg?style=for-the-badge&logo=vue.js" alt="Vue.js" />
  </a>
  <a href="https://spring.io/projects/spring-boot" target="_blank">
    <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F.svg?style=for-the-badge&logo=spring-boot" alt="Spring Boot" />
  </a>
  <a href="https://www.mysql.com/" target="_blank">
    <img src="https://img.shields.io/badge/MySQL-8.0-4479A1.svg?style=for-the-badge&logo=mysql" alt="MySQL" />
  </a>
  <a href="https://redis.io/" target="_blank">
    <img src="https://img.shields.io/badge/Redis-7.0-DC382D.svg?style=for-the-badge&logo=redis" alt="Redis" />
  </a>
  <a href="https://www.mongodb.com/" target="_blank">
    <img src="https://img.shields.io/badge/MongoDB-5.0-47A248.svg?style=for-the-badge&logo=mongodb" alt="MongoDB" />
  </a>
  <a href="https://www.rabbitmq.com/" target="_blank">
    <img src="https://img.shields.io/badge/RabbitMQ-3.10-FF6600.svg?style=for-the-badge&logo=rabbitmq" alt="RabbitMQ" />
  </a>
  <a href="https://element-plus.org/" target="_blank">
    <img src="https://img.shields.io/badge/Element%20Plus-2.x-409EFF.svg?style=for-the-badge&logo=element" alt="Element Plus" />
  </a>
</div>

<div align="center" style="margin-top: 10px;">
  <a href="https://opensource.org/licenses/MIT" target="_blank">
    <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square" alt="License: MIT" />
  </a>
  <a href="https://github.com/yourusername/gobang/stargazers" target="_blank">
    <img src="https://img.shields.io/github/stars/yourusername/gobang.svg?style=social" alt="GitHub stars" />
  </a>
  <a href="https://github.com/yourusername/gobang/network" target="_blank">
    <img src="https://img.shields.io/github/forks/yourusername/gobang.svg?style=social" alt="GitHub forks" />
  </a>
  <img src="https://img.shields.io/badge/Status-Active-green.svg?style=flat-square" alt="Status: Active" />
  <img src="https://img.shields.io/badge/Maintenance-Yes-green.svg?style=flat-square" alt="Maintenance: Yes" />
  <img src="https://img.shields.io/badge/Version-2.0.0-blue.svg?style=flat-square" alt="Version: 2.0.0" />
</div>

## 项目简介

这是一个基于前后端分离架构的在线多棋类游戏平台，支持五子棋、围棋、飞行棋、象棋、军棋等多种棋类游戏，提供玩家注册登录、创建房间、随机匹配、实时对战、积分系统、排行榜等完整功能，具备商用级别的稳定性和扩展性。

## 技术栈

### 前端
- **框架**: Vue 3 + Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **UI组件**: Element Plus
- **网络请求**: Axios
- **WebSocket**: 原生WebSocket API
- **实时通信**: Socket.IO

### 后端
- **框架**: Spring Boot 3.x
- **持久层**: MyBatis Plus
- **关系型数据库**: MySQL 8.0
- **非关系型数据库**: MongoDB
- **消息队列**: RabbitMQ
- **实时通信**: WebSocket / Socket.IO
- **缓存**: Redis
- **安全框架**: Spring Security + JWT
- **构建工具**: Maven
- **分布式锁**: Redisson

### 开发工具
- **IDE**: IntelliJ IDEA / VS Code
- **版本控制**: Git
- **CI/CD**: Jenkins
- **监控**: Prometheus + Grafana
- **日志**: ELK Stack

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
#### 五子棋
- 15x15标准棋盘
- 落子逻辑与胜负判断
- 悔棋与求和功能
- 实时计时器

#### 围棋
- 19x19标准棋盘
- 落子规则与提子逻辑
- 胜负计算（目数/数子）
- 悔棋与认输功能

#### 飞行棋
- 标准飞行棋棋盘
- 骰子随机系统
- 跳跃与碰撞规则
- 多人对战支持

#### 象棋
- 中国象棋标准棋盘
- 各棋子走法规则
- 将军与胜负判断
- 悔棋功能

#### 军棋
- 标准军棋棋盘
- 暗棋与明棋模式
- 棋子大小规则
- 军旗夺取机制

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
- JDK 17+
- MySQL 8.0+
- MongoDB 5.0+
- Redis 7.0+
- RabbitMQ 3.10+
- Node.js 18+
- npm 9+

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
- `PUT /api/user/update` - 更新用户信息
- `POST /api/user/avatar` - 上传头像

### 游戏接口
- `POST /api/game/create` - 创建房间
- `GET /api/game/rooms` - 获取房间列表
- `POST /api/game/join` - 加入房间
- `POST /api/game/leave` - 离开房间
- `GET /api/game/info` - 获取游戏信息
- `POST /api/game/move` - 落子操作
- `POST /api/game/undo` - 悔棋请求
- `POST /api/game/surrender` - 认输

### 匹配接口
- `POST /api/match/start` - 开始匹配
- `POST /api/match/cancel` - 取消匹配
- `GET /api/match/status` - 获取匹配状态

### 排行榜接口
- `GET /api/ranking/list` - 获取排行榜
- `GET /api/ranking/user` - 获取用户排名

### 消息接口
- `GET /api/message/list` - 获取消息列表
- `POST /api/message/send` - 发送消息
- `PUT /api/message/read` - 标记已读

### WebSocket接口
- 连接地址: `ws://localhost:8080/ws/game`
- 消息格式: JSON
- 支持游戏实时通信、聊天、通知等功能

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

#### Docker部署
```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d
```

#### 手动部署

##### 后端部署
```bash
cd backend
mvn clean package -DskipTests
java -jar target/game-platform-0.0.1-SNAPSHOT.jar
```

##### 前端部署
```bash
cd frontend
npm run build
# 将dist目录部署到Nginx或其他静态文件服务器
```

##### 配置文件
生产环境需要修改以下配置文件：
- `backend/src/main/resources/application-prod.yml` - 后端生产配置
- `frontend/.env.production` - 前端生产环境变量

## 监控与维护

### 1. 监控系统
- **服务监控**: Prometheus + Grafana
- **日志收集**: ELK Stack
- **链路追踪**: Zipkin
- **性能监控**: JVM监控 + 数据库监控

### 2. 日志
- 后端日志: `logs/game-platform.log`
- 前端日志: 浏览器控制台 + Sentry错误监控

### 3. 常见问题
- 连接失败: 检查数据库、Redis、RabbitMQ配置和端口占用
- 匹配超时: 检查WebSocket连接和消息队列状态
- 游戏卡顿: 检查网络延迟、服务器负载和数据库性能
- 数据不一致: 检查事务配置和消息队列可靠性

## 贡献者

感谢所有为这个项目做出贡献的开发者！

| 贡献者 | GitHub | 贡献内容 |
|-------|--------|----------|
| [Your Name] | [yourusername](https://github.com/yourusername) | 项目发起人，核心功能开发 |
| [Contributor 1] | [contributor1](https://github.com/contributor1) | 前端组件开发 |
| [Contributor 2] | [contributor2](https://github.com/contributor2) | 后端API实现 |
| [Contributor 3] | [contributor3](https://github.com/contributor3) | 游戏逻辑优化 |

## 项目成员

| 角色 | 姓名 | GitHub | 联系方式 |
|-----|------|--------|----------|
| 项目负责人/核心开发 | [Your Name] | [yourusername](https://github.com/yourusername) | [your.email@example.com](mailto:your.email@example.com) |
| 前端开发 | [Frontend Dev] | [frontend-dev](https://github.com/frontend-dev) | [frontend.dev@example.com](mailto:frontend.dev@example.com) |
| 后端开发 | [Backend Dev] | [backend-dev](https://github.com/backend-dev) | [backend.dev@example.com](mailto:backend.dev@example.com) |
| UI/UX设计 | [Designer] | [designer](https://github.com/designer) | [designer@example.com](mailto:designer@example.com) |

## 许可证

MIT License

## 更新日志

### v2.0.0 (2024-XX-XX)
- 升级技术栈到Spring Boot 3.x和Vue 3
- 增加多游戏支持（五子棋、围棋、飞行棋、象棋、军棋）
- 引入MyBatis Plus、MongoDB、RabbitMQ等中间件
- 实现分布式部署架构
- 完善用户系统和积分排名

### v1.0.0 (2024-12-XX)
- 项目初始化
- 实现前后端分离架构
- 完成用户注册登录功能
- 实现游戏大厅和房间系统
- 完成五子棋核心游戏逻辑
