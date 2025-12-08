# Come 下棋 - 多棋类在线对战平台

![GitHub license](https://img.shields.io/github/license/phoenix-cities/gobang)
![Gitee stars](https://gitee.com/phoenix-cities/gobang/badge/star.svg?theme=dark)
![Gitee forks](https://gitee.com/phoenix-cities/gobang/badge/fork.svg?theme=dark)
![Gitee issues](https://gitee.com/phoenix-cities/gobang/badge/issue.svg?theme=dark)
![Gitee pull requests](https://gitee.com/phoenix-cities/gobang/badge/pr.svg?theme=dark)
![Gitee release](https://gitee.com/phoenix-cities/gobang/badge/release.svg?theme=dark)
![Vue.js](https://img.shields.io/badge/vue.js-3.x-green)
![Spring Boot](https://img.shields.io/badge/spring%20boot-2.7.x-brightgreen)
![MySQL](https://img.shields.io/badge/mysql-8.0-blue)
![Redis](https://img.shields.io/badge/redis-6.0-red)

一个支持多种棋类游戏的在线对战平台，包括五子棋、军棋、象棋等。采用前后端分离架构，支持实时对战、用户匹配、积分系统等功能。

## 🎮 功能特性

### 核心功能
- **多种棋类游戏**: 五子棋、军棋、象棋等
- **实时对战**: WebSocket 实现实时游戏通信
- **智能匹配**: 基于积分的智能匹配系统
- **用户系统**: 注册、登录、用户信息管理
- **积分系统**: 胜负积分变化、排行榜
- **游戏回放**: 支持游戏复盘和步骤查看

### 技术特色
- **前后端分离**: Vue.js + Spring Boot
- **实时通信**: WebSocket 协议
- **数据安全**: JWT 认证、密码加密
- **高并发**: Redis 缓存、连接池优化
- **可扩展**: 模块化设计、易于添加新棋类

## 🏗️ 技术架构

### 前端技术栈
- **框架**: Vue.js 3 + TypeScript
- **UI 组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP 客户端**: Axios
- **WebSocket**: 原生 WebSocket API
- **构建工具**: Vite

### 后端技术栈
- **框架**: Spring Boot 2.7.x
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **认证**: Spring Security + JWT
- **数据访问**: MyBatis Plus
- **实时通信**: Spring WebSocket
- **文档**: Swagger/OpenAPI
- **测试**: JUnit 5 + TestContainers

### 部署架构
- **容器化**: Docker + Docker Compose
- **负载均衡**: Nginx
- **监控**: Spring Boot Actuator
- **日志**: Logback + ELK

## 🚀 快速开始

### 环境要求
- **Java**: 11 或更高版本
- **Node.js**: 14.x 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 6.0 或更高版本

### 1. 克隆项目
```bash
git clone https://gitee.com/phoenix-cities/gobang.git
cd gobang
```

### 2. 数据库初始化
```bash
# 登录 MySQL
mysql -u root -p

# 执行数据库初始化脚本
source database_schema.sql
```

### 3. 后端配置和启动
```bash
# 进入后端目录
cd backend

# 安装依赖
mvn clean install

# 配置文件
# 编辑 src/main/resources/application.yml
# 修改数据库连接、Redis 配置等

# 启动应用
mvn spring-boot:run
```

### 4. 前端配置和启动
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 配置文件
# 编辑 .env 文件，配置 API 地址

# 启动开发服务器
npm run dev
```

### 5. 访问应用
- **前端地址**: http://localhost:3000
- **后端地址**: http://localhost:8080
- **API 文档**: http://localhost:8080/swagger-ui.html

## 📖 API 文档

详细的 API 文档请参考 [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

### 主要接口分类
- **用户管理**: 注册、登录、用户信息
- **游戏匹配**: 加入匹配、取消匹配
- **游戏房间**: 创建房间、加入房间、游戏操作
- **积分系统**: 积分变化、排行榜
- **系统配置**: 获取配置、系统状态

## 🗄️ 数据库设计

### 核心表结构
- **user**: 用户信息表
- **game_room**: 游戏房间表
- **game_record**: 游戏记录表
- **game_step**: 游戏步骤表
- **user_session**: 用户会话表
- **user_score_log**: 积分变化记录表
- **system_config**: 系统配置表

详细的数据库结构请参考 [database_schema.sql](database_schema.sql)

## 🔧 配置说明

### 后端配置 (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/java_gobang
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
  
  jwt:
    secret: your_jwt_secret_key
    expiration: 86400000
```

### 前端配置 (.env)
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=ws://localhost:8080
VITE_APP_NAME=Come下棋
```

## 🐳 Docker 部署

### 使用 Docker Compose
```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 单独构建
```bash
# 构建后端镜像
docker build -t gobang-backend ./backend

# 构建前端镜像
docker build -t gobang-frontend ./frontend

# 运行容器
docker run -d -p 8080:8080 --name gobang-backend gobang-backend
docker run -d -p 3000:80 --name gobang-frontend gobang-frontend
```

## 🧪 测试

### 后端测试
```bash
# 单元测试
mvn test

# 集成测试
mvn verify

# 代码覆盖率
mvn jacoco:report
```

### 前端测试
```bash
# 单元测试
npm run test

# 端到端测试
npm run test:e2e

# 代码质量检查
npm run lint
```

## 📊 性能优化

### 数据库优化
- 合理设计索引
- 使用连接池
- 分表分库策略
- 读写分离

### 缓存策略
- Redis 缓存热点数据
- 本地缓存用户会话
- 游戏状态缓存
- 排行榜缓存

### 前端优化
- 组件懒加载
- 图片资源优化
- CDN 加速
- 代码分割

## 🔒 安全考虑

### 认证授权
- JWT Token 认证
- 密码加密存储
- 会话管理
- 权限控制

### 数据安全
- SQL 注入防护
- XSS 攻击防护
- CSRF 防护
- 数据加密传输

### 系统安全
- 接口限流
- 防重放攻击
- 日志审计
- 异常监控

## 🤝 贡献指南

1. Fork 项目 (https://gitee.com/phoenix-cities/gobang)
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### 开发规范
- 遵循代码规范
- 编写单元测试
- 更新文档
- 通过代码审查

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🆘 问题反馈

- **Issues**: [项目 Issues](https://gitee.com/phoenix-cities/gobang/issues)
- **讨论**: [项目讨论](https://gitee.com/phoenix-cities/gobang/issues/new)
- **邮箱**: support@come-xiaqi.com

## 📞 联系我们

- **项目主页**: https://come-xiaqi.com
- **技术博客**: https://blog.come-xiaqi.com
- **官方QQ群**: 123456789
- **微信公众号**: Come下棋

---

⭐ 如果这个项目对你有帮助，请给个 Star！