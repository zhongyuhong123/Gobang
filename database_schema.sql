-- 创建数据库
CREATE DATABASE IF NOT EXISTS `java_gobang` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `java_gobang`;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `userId` INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(20) NOT NULL COMMENT '密码',
    `score` INT DEFAULT 1000 COMMENT '积分',
    `totalCount` INT DEFAULT 0 COMMENT '总比赛场次',
    `winCount` INT DEFAULT 0 COMMENT '获胜场次',
    `createTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建房间表
CREATE TABLE IF NOT EXISTS `room` (
    `roomId` INT PRIMARY KEY AUTO_INCREMENT COMMENT '房间ID',
    `userId1` INT COMMENT '玩家1ID',
    `userId2` INT COMMENT '玩家2ID',
    `createTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `status` INT DEFAULT 0 COMMENT '房间状态：0-等待中，1-游戏中，2-已结束',
    FOREIGN KEY (`userId1`) REFERENCES `user`(`userId`) ON DELETE SET NULL,
    FOREIGN KEY (`userId2`) REFERENCES `user`(`userId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间表';

-- 创建游戏记录表
CREATE TABLE IF NOT EXISTS `game_record` (
    `recordId` INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `roomId` INT COMMENT '房间ID',
    `winnerId` INT COMMENT '获胜者ID',
    `loserId` INT COMMENT '失败者ID',
    `startTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `endTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间',
    `totalSteps` INT DEFAULT 0 COMMENT '总步数',
    FOREIGN KEY (`roomId`) REFERENCES `room`(`roomId`) ON DELETE SET NULL,
    FOREIGN KEY (`winnerId`) REFERENCES `user`(`userId`) ON DELETE SET NULL,
    FOREIGN KEY (`loserId`) REFERENCES `user`(`userId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏记录表';

-- 创建游戏步骤表
CREATE TABLE IF NOT EXISTS `game_step` (
    `stepId` INT PRIMARY KEY AUTO_INCREMENT COMMENT '步骤ID',
    `recordId` INT COMMENT '游戏记录ID',
    `userId` INT COMMENT '落子玩家ID',
    `row` INT NOT NULL COMMENT '落子行坐标',
    `col` INT NOT NULL COMMENT '落子列坐标',
    `stepTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '落子时间',
    `stepOrder` INT DEFAULT 0 COMMENT '步数顺序',
    FOREIGN KEY (`recordId`) REFERENCES `game_record`(`recordId`) ON DELETE CASCADE,
    FOREIGN KEY (`userId`) REFERENCES `user`(`userId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏步骤表';

-- 创建索引
CREATE INDEX idx_user_username ON `user`(`username`);
CREATE INDEX idx_room_status ON `room`(`status`);
CREATE INDEX idx_game_record_winner ON `game_record`(`winnerId`);
CREATE INDEX idx_game_step_record ON `game_step`(`recordId`);
