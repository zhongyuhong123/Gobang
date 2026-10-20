package com.yangshengzhou.gobang.game;

import java.util.Map;

//客户端连接到游戏房间后，服务器返回的响应
public class GameReadyResponse {
    private String message;
    private boolean ok;
    private String  reason;
    private String roomId;
    private int thisUserId;
    private int thatUserId;
    private String thatUsername;
    private int whiteUser;
    /** 棋种：gobang / military / chinese-chess */
    private String gameType;
    /** 开局局面 */
    private Map<String, Object> state;

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public Map<String, Object> getState() {
        return state;
    }

    public void setState(Map<String, Object> state) {
        this.state = state;
    }

    public String getThatUsername() {
        return thatUsername;
    }

    public void setThatUsername(String thatUsername) {
        this.thatUsername = thatUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getThisUserId() {
        return thisUserId;
    }

    public void setThisUserId(int thisUserId) {
        this.thisUserId = thisUserId;
    }

    public int getThatUserId() {
        return thatUserId;
    }

    public void setThatUserId(int thatUserId) {
        this.thatUserId = thatUserId;
    }

    public int getWhiteUser() {
        return whiteUser;
    }

    public void setWhiteUser(int whiteUser) {
        this.whiteUser = whiteUser;
    }
}