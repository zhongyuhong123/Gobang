package com.yangshengzhou.gobang.game;

//这表示一个websocket的匹配请求
public class MatchRequest {
    private String message = "";
    /** 棋种：gobang / military / chinese-chess */
    private String gameMode = "gobang";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
}
