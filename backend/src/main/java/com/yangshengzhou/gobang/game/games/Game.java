package com.yangshengzhou.gobang.game.games;

import com.fasterxml.jackson.databind.JsonNode;
import com.yangshengzhou.gobang.entity.User;

import java.util.Map;

/**
 * 一种棋的规则引擎。Room 只负责房间生命周期与消息收发，具体规则都放在实现类里。
 */
public interface Game {

    /** 棋种 id：gobang / military / chinese-chess */
    String getType();

    /** 双方就位后初始化棋盘 */
    void init(User user1, User user2);

    /**
     * 尝试执行一步。
     *
     * @return null 表示成功；否则返回给操作者的错误提示
     */
    String applyMove(User player, JsonNode request);

    /** 成功落子后广播给双方的消息体 */
    Map<String, Object> buildUpdate(User mover);

    /** 开局局面（放在 gameReady 里下发） */
    Map<String, Object> initialState();

    /** 当前该谁走 */
    int getTurnUserId();

    boolean isFinished();

    /** 赢家 id；和棋或未结束返回 null */
    Integer getWinnerId();

    /** 结束原因 */
    String getEndReason();
}
