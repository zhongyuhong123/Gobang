package com.yangshengzhou.gobang.game.games;

import com.yangshengzhou.gobang.entity.User;

/**
 * 棋种工厂 / 别名归一化。
 */
public final class GameFactory {

    private GameFactory() {
    }

    public static final String GOBANG = "gobang";
    public static final String MILITARY = "military";
    public static final String CHINESE_CHESS = "chinese-chess";

    /** 兼容前端/数据库里出现的各种写法 */
    public static String normalize(String mode) {
        if (mode == null) {
            return GOBANG;
        }
        String m = mode.trim().toLowerCase();
        switch (m) {
            case "military":
            case "junqi":
            case "army":
                return MILITARY;
            case "chinese-chess":
            case "chinese_chess":
            case "xiangqi":
            case "chess":
                return CHINESE_CHESS;
            case "gobang":
            case "five":
            default:
                return GOBANG;
        }
    }

    public static Game create(String mode, User user1, User user2) {
        String type = normalize(mode);
        Game game;
        switch (type) {
            case MILITARY:
                game = new JunqiGame();
                break;
            case CHINESE_CHESS:
                game = new XiangqiGame();
                break;
            case GOBANG:
            default:
                game = new GobangGame();
                break;
        }
        game.init(user1, user2);
        return game;
    }
}
