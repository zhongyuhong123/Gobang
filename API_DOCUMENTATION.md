# Come 下棋 - API 接口文档

## 概述
本文档描述了Come下棋平台的RESTful API接口规范，支持用户管理、游戏匹配、游戏对战等核心功能。

## 基础信息
- **Base URL**: `http://localhost:8080`
- **认证方式**: Session-based
- **数据格式**: JSON
- **编码**: UTF-8

## 通用响应格式
所有API响应都遵循统一的格式：

```json
{
  "ok": true,
  "reason": "",
  "data": {}
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|--------|------|------|
| ok | boolean | 请求是否成功 |
| reason | string | 错误原因（成功时为空） |
| data | object | 响应数据 |

## 用户管理 API

### 1. 用户登录
**POST** `/login`

用户登录接口，验证用户名和密码。

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

#### 请求示例
```json
{
  "username": "player1",
  "password": "123456"
}
```

#### 响应示例
成功响应：
```json
{
  "ok": true,
  "reason": "",
  "data": {
    "userId": 1,
    "username": "player1",
    "score": 1000
  }
}
```

失败响应：
```json
{
  "ok": false,
  "reason": "用户名或密码错误",
  "data": {}
}
```

### 2. 用户注册
**POST** `/register`

新用户注册接口。

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名（唯一） |
| password | string | 是 | 密码 |

#### 请求示例
```json
{
  "username": "newplayer",
  "password": "123456"
}
```

#### 响应示例
成功响应：
```json
{
  "ok": true,
  "reason": "",
  "data": {
    "userId": 2,
    "username": "newplayer"
  }
}
```

失败响应：
```json
{
  "ok": false,
  "reason": "用户名已存在",
  "data": {}
}
```

### 3. 获取用户信息
**GET** `/userInfo`

获取当前登录用户的信息。

#### 请求参数
无（需要session验证）

#### 响应示例
```json
{
  "ok": true,
  "reason": "",
  "data": {
    "userId": 1,
    "username": "player1",
    "score": 1000,
    "totalGames": 50,
    "winGames": 25
  }
}
```

## WebSocket API

### 1. 游戏匹配 WebSocket
**WebSocket连接**: `ws://localhost:8080/gameHall`

用于游戏匹配功能，支持开始匹配和取消匹配。

#### 消息格式
客户端发送消息：
```json
{
  "message": "startMatch"  // 或 "stopMatch"
}
```

服务器响应消息：
```json
{
  "ok": true,
  "message": "startMatch",
  "reason": ""
}
```

#### 连接建立流程
1. 客户端通过WebSocket连接到 `/gameHall`
2. 服务器验证用户登录状态
3. 验证用户是否已在其他位置登录（防止多开）
4. 将用户标记为在线状态

#### 匹配流程
1. 客户端发送 `startMatch` 消息
2. 服务器将用户加入匹配队列
3. 匹配器自动匹配对手
4. 匹配成功后，服务器创建游戏房间
5. 双方玩家收到匹配成功通知

### 2. 游戏房间 WebSocket
**WebSocket连接**: `ws://localhost:8080/game`

用于游戏对战，处理落子、游戏状态同步等。

#### 连接建立流程
1. 玩家通过WebSocket连接到 `/game`
2. 服务器验证用户登录状态
3. 验证用户是否已匹配到对手
4. 验证是否多开游戏页面
5. 将用户标记为游戏房间在线状态
6. 当双方都连接成功后，通知游戏开始

#### 游戏消息格式

**落子消息**（客户端发送）：
```json
{
  "type": "putChess",
  "userId": 1,
  "position": {
    "row": 10,
    "col": 10
  }
}
```

**游戏状态更新**（服务器推送）：
```json
{
  "message": "putChess",
  "userId": 1,
  "winner": 1,  // 胜利者ID，没有胜利者为null
  "position": {
    "row": 10,
    "col": 10
  }
}
```

**游戏准备通知**：
```json
{
  "message": "gameReady",
  "ok": true,
  "roomId": "room123",
  "thisUserId": 1,
  "thatUserId": 2,
  "whiteUser": 1  // 先手玩家ID
}
```

## 游戏逻辑 API

### 1. 游戏房间管理

#### 房间状态
- **WAITING**: 等待第二个玩家加入
- **READY**: 双方玩家都已连接，游戏即将开始
- **PLAYING**: 游戏进行中
- **FINISHED**: 游戏结束

#### 玩家状态
- **ONLINE**: 在线但未匹配
- **MATCHING**: 匹配中
- **READY**: 已匹配，等待游戏开始
- **PLAYING**: 游戏中
- **OFFLINE**: 离线

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户未登录 |
| 1002 | 用户名或密码错误 |
| 1003 | 用户名已存在 |
| 2001 | 用户已在其他位置登录 |
| 2002 | 用户尚未匹配到对手 |
| 2003 | 房间已满 |
| 3001 | 非法的匹配请求 |
| 3002 | 游戏房间连接异常 |
| 4001 | 数据库操作失败 |

## 数据模型

### User 用户模型
```json
{
  "userId": 1,
  "username": "player1",
  "password": "encrypted_password",
  "score": 1000,
  "totalGames": 50,
  "winGames": 25,
  "createTime": "2024-01-01 10:00:00"
}
```

### Room 房间模型
```json
{
  "roomId": "room123",
  "user1": {},  // User对象
  "user2": {},  // User对象
  "whiteUser": {},  // 先手玩家User对象
  "board": [],  // 棋盘状态
  "currentUser": {},  // 当前回合玩家
  "winner": null,  // 胜利者
  "createTime": "2024-01-01 10:00:00"
}
```

### ChessPosition 落子位置
```json
{
  "row": 10,
  "col": 10
}
```

## 安全说明

1. **密码加密**: 用户密码使用BCrypt加密存储
2. **Session管理**: 使用Spring Session进行会话管理
3. **防多开**: 同一账号不能同时在多个地方登录
4. **输入验证**: 所有用户输入都进行严格验证
5. **SQL注入防护**: 使用MyBatis参数化查询

## 性能指标

- **响应时间**: 99%的请求响应时间 < 200ms
- **并发支持**: 支持1000+并发WebSocket连接
- **匹配时间**: 平均匹配时间 < 5秒
- **可用性**: 99.9%的系统可用性

## 版本历史

### v1.0.0 (2024-01-01)
- 初始版本发布
- 支持用户注册登录
- 支持五子棋游戏匹配和对战
- WebSocket实时通信

## 联系方式
- **技术支持**: support@comegame.com
- **文档更新**: https://github.com/comegame/gobang/wiki