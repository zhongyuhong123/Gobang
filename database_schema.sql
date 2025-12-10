-- Come 下棋 - 数据库初始化脚本
-- 版本: v1.0.0
-- 描述: 多棋类在线对战平台数据库结构
-- 创建时间: 2024-01-01

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `gobang` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `gobang`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `user_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(30) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱地址',
    `nickname` VARCHAR(50) COMMENT '用户昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `score` INT DEFAULT 1000 COMMENT '积分',
    `level` INT DEFAULT 1 COMMENT '等级',
    `total_games` INT DEFAULT 0 COMMENT '总比赛场次',
    `win_games` INT DEFAULT 0 COMMENT '获胜场次',
    `lose_games` INT DEFAULT 0 COMMENT '失败场次',
    `draw_games` INT DEFAULT 0 COMMENT '平局场次',
    `win_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '胜率',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `last_login_time` TIMESTAMP NULL COMMENT '最后登录时间',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_username` (`username`),
    INDEX `idx_score` (`score`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 游戏房间表
CREATE TABLE IF NOT EXISTS `game_room` (
    `room_id` VARCHAR(32) PRIMARY KEY COMMENT '房间ID',
    `room_name` VARCHAR(100) COMMENT '房间名称',
    `game_type` VARCHAR(20) NOT NULL COMMENT '游戏类型：gobang,military,chinese_chess',
    `user_id1` INT COMMENT '玩家1ID',
    `user_id2` INT COMMENT '玩家2ID',
    `current_player` INT COMMENT '当前玩家ID',
    `winner_id` INT COMMENT '胜利者ID',
    `board_state` TEXT COMMENT '棋盘状态（JSON格式）',
    `game_config` JSON COMMENT '游戏配置',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-等待中，1-游戏中，2-已结束，3-异常',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `start_time` TIMESTAMP NULL COMMENT '开始时间',
    `end_time` TIMESTAMP NULL COMMENT '结束时间',
    `last_action_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后操作时间',
    
    FOREIGN KEY (`user_id1`) REFERENCES `user`(`user_id`) ON DELETE SET NULL,
    FOREIGN KEY (`user_id2`) REFERENCES `user`(`user_id`) ON DELETE SET NULL,
    FOREIGN KEY (`winner_id`) REFERENCES `user`(`user_id`) ON DELETE SET NULL,
    
    INDEX `idx_game_type` (`game_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_last_action` (`last_action_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏房间表';

-- 游戏记录表
CREATE TABLE IF NOT EXISTS `game_record` (
    `record_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `room_id` VARCHAR(32) NOT NULL COMMENT '房间ID',
    `game_type` VARCHAR(20) NOT NULL COMMENT '游戏类型',
    `player1_id` INT COMMENT '玩家1ID',
    `player2_id` INT COMMENT '玩家2ID',
    `winner_id` INT COMMENT '胜利者ID',
    `loser_id` INT COMMENT '失败者ID',
    `is_draw` BOOLEAN DEFAULT FALSE COMMENT '是否平局',
    `total_steps` INT DEFAULT 0 COMMENT '总步数',
    `game_duration` INT COMMENT '游戏时长（秒）',
    `player1_score_change` INT DEFAULT 0 COMMENT '玩家1积分变化',
    `player2_score_change` INT DEFAULT 0 COMMENT '玩家2积分变化',
    `replay_data` JSON COMMENT '复盘数据',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (`player1_id`) REFERENCES `user`(`user_id`) ON DELETE SET NULL,
    FOREIGN KEY (`player2_id`) REFERENCES `user`(`user_id`) ON DELETE SET NULL,
    FOREIGN KEY (`winner_id`) REFERENCES `user`(`user_id`) ON DELETE SET NULL,
    FOREIGN KEY (`loser_id`) REFERENCES `user`(`user_id`) ON DELETE SET NULL,
    
    INDEX `idx_room_id` (`room_id`),
    INDEX `idx_game_type` (`game_type`),
    INDEX `idx_winner` (`winner_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏记录表';

-- 游戏步骤表
CREATE TABLE IF NOT EXISTS `game_step` (
    `step_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '步骤ID',
    `record_id` BIGINT NOT NULL COMMENT '游戏记录ID',
    `room_id` VARCHAR(32) NOT NULL COMMENT '房间ID',
    `user_id` INT NOT NULL COMMENT '落子玩家ID',
    `step_order` INT NOT NULL COMMENT '步数顺序',
    `position_row` INT NOT NULL COMMENT '落子行坐标',
    `position_col` INT NOT NULL COMMENT '落子列坐标',
    `piece_type` VARCHAR(10) COMMENT '棋子类型',
    `action_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '落子时间',
    `board_state` TEXT COMMENT '落子后的棋盘状态',
    
    FOREIGN KEY (`record_id`) REFERENCES `game_record`(`record_id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE,
    
    INDEX `idx_record_id` (`record_id`),
    INDEX `idx_room_id` (`room_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_step_order` (`step_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏步骤表';

-- 用户会话表
CREATE TABLE IF NOT EXISTS `user_session` (
    `session_id` VARCHAR(64) PRIMARY KEY COMMENT '会话ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `login_ip` VARCHAR(45) COMMENT '登录IP',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `last_activity` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后活动时间',
    `expire_time` TIMESTAMP NOT NULL COMMENT '过期时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-有效，0-无效',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE,
    
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_expire_time` (`expire_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话表';

-- 用户积分变化记录表
CREATE TABLE IF NOT EXISTS `user_score_log` (
    `log_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `score_change` INT NOT NULL COMMENT '积分变化',
    `score_before` INT NOT NULL COMMENT '变化前积分',
    `score_after` INT NOT NULL COMMENT '变化后积分',
    `change_type` VARCHAR(20) NOT NULL COMMENT '变化类型：game_win,game_lose,game_draw,admin_adjust',
    `related_id` VARCHAR(32) COMMENT '关联ID（房间ID或管理员操作ID）',
    `description` VARCHAR(200) COMMENT '描述',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE,
    
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_change_type` (`change_type`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分变化记录表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `config_key` VARCHAR(50) PRIMARY KEY COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(20) DEFAULT 'string' COMMENT '配置类型：string,int,bool,json',
    `description` VARCHAR(200) COMMENT '配置描述',
    `is_public` BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入默认系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `is_public`) VALUES
('site_name', 'Come下棋', 'string', '网站名称', true),
('site_description', '多棋类在线对战平台', 'string', '网站描述', true),
('max_game_duration', '3600', 'int', '最大游戏时长（秒）', false),
('match_timeout', '30', 'int', '匹配超时时间（秒）', false),
('initial_score', '1000', 'int', '初始积分', false),
('score_win', '10', 'int', '胜利获得积分', false),
('score_lose', '-5', 'int', '失败扣除积分', false),
('score_draw', '2', 'int', '平局获得积分', false),
('enable_registration', 'true', 'bool', '是否开启注册', true),
('max_online_users', '1000', 'int', '最大在线用户数', false);

-- 插入测试用户（密码：123456，已加密）
INSERT INTO `user` (`username`, `password`, `email`, `nickname`, `score`, `total_games`, `win_games`) VALUES
('testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE8wv4f3M.qXc0wJO', 'test1@example.com', '测试用户1', 1200, 50, 30),
('testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE8wv4f3M.qXc0wJO', 'test2@example.com', '测试用户2', 800, 40, 15),
('testuser3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE8wv4f3M.qXc0wJO', 'test3@example.com', '测试用户3', 1500, 80, 60);

-- 创建定时任务清理过期会话
CREATE EVENT IF NOT EXISTS `clean_expired_sessions`
ON SCHEDULE EVERY 1 HOUR
DO
    DELETE FROM `user_session` WHERE `expire_time` < NOW();

-- 创建定时任务清理过期房间
CREATE EVENT IF NOT EXISTS `clean_expired_rooms`
ON SCHEDULE EVERY 30 MINUTE
DO
    DELETE FROM `game_room` 
    WHERE `status` = 0 
    AND `create_time` < DATE_SUB(NOW(), INTERVAL 2 HOUR);

-- 创建视图：用户统计信息
CREATE OR REPLACE VIEW `user_statistics` AS
SELECT 
    u.user_id,
    u.username,
    u.nickname,
    u.score,
    u.level,
    u.total_games,
    u.win_games,
    u.lose_games,
    u.draw_games,
    CASE 
        WHEN u.total_games > 0 THEN ROUND((u.win_games * 100.0 / u.total_games), 2)
        ELSE 0.00
    END AS win_rate,
    u.last_login_time,
    u.create_time
FROM `user` u
WHERE u.status = 1;

-- 创建视图：活跃用户排行
CREATE OR REPLACE VIEW `active_users_ranking` AS
SELECT 
    u.user_id,
    u.username,
    u.nickname,
    u.score,
    u.level,
    COUNT(DISTINCT gr.record_id) AS recent_games,
    MAX(gr.create_time) AS last_game_time
FROM `user` u
LEFT JOIN `game_record` gr ON u.user_id IN (gr.player1_id, gr.player2_id)
    AND gr.create_time > DATE_SUB(NOW(), INTERVAL 7 DAY)
WHERE u.status = 1
GROUP BY u.user_id
ORDER BY u.score DESC, recent_games DESC;

-- 创建视图：游戏统计
CREATE OR REPLACE VIEW `game_statistics` AS
SELECT 
    game_type,
    COUNT(*) AS total_games,
    COUNT(CASE WHEN is_draw = TRUE THEN 1 END) AS draw_games,
    AVG(total_steps) AS avg_steps,
    AVG(game_duration) AS avg_duration,
    DATE(create_time) AS game_date
FROM `game_record`
GROUP BY game_type, DATE(create_time)
ORDER BY game_date DESC;

-- 权限设置
-- 创建用户（如果不存在）
CREATE USER IF NOT EXISTS 'gobang_user'@'localhost' IDENTIFIED BY 'gobang_password';

-- 授权
GRANT SELECT, INSERT, UPDATE, DELETE ON `java_gobang`.* TO 'gobang_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 最后更新时间
SELECT '数据库初始化完成' AS message, NOW() AS init_time;