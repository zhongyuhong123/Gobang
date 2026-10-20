package com.yangshengzhou.gobang.game.games;

import com.fasterxml.jackson.databind.JsonNode;
import com.yangshengzhou.gobang.entity.User;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 五子棋：15x15，先连成 5 子（含 5 子以上）者胜，棋盘下满为和棋。
 */
public class GobangGame implements Game {

    public static final int SIZE = 15;
    private static final int WIN_COUNT = 5;

    private final int[][] board = new int[SIZE][SIZE];
    private User user1;
    private User user2;
    private int moveCount = 0;
    private int lastRow = -1;
    private int lastCol = -1;
    private int lastUserId = 0;
    private Integer winnerId = null;
    private String endReason = null;
    private boolean finished = false;

    @Override
    public String getType() {
        return "gobang";
    }

    @Override
    public void init(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    @Override
    public String applyMove(User player, JsonNode request) {
        if (finished) {
            return "本局已经结束";
        }
        if (player == null || (player.getUserId() != user1.getUserId() && player.getUserId() != user2.getUserId())) {
            return "你不在这局游戏里";
        }
        if (player.getUserId() != getTurnUserId()) {
            return "还没轮到你下棋";
        }

        int row = request.path("row").asInt(-1);
        int col = request.path("col").asInt(-1);
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return "落子位置越界";
        }
        if (board[row][col] != 0) {
            return "该位置已经有棋子了";
        }

        int chess = (player.getUserId() == user1.getUserId()) ? 1 : 2;
        board[row][col] = chess;
        moveCount++;
        lastRow = row;
        lastCol = col;
        lastUserId = player.getUserId();

        if (checkWinner(row, col, chess)) {
            winnerId = player.getUserId();
            endReason = "五子连珠";
            finished = true;
        } else if (moveCount == SIZE * SIZE) {
            winnerId = null;
            endReason = "棋盘已满，平局";
            finished = true;
        }
        return null;
    }

    @Override
    public Map<String, Object> buildUpdate(User mover) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("message", "putChess");
        msg.put("gameType", getType());
        msg.put("userId", lastUserId);
        msg.put("row", lastRow);
        msg.put("col", lastCol);
        msg.put("winner", winnerId == null ? 0 : winnerId);
        return msg;
    }

    @Override
    public Map<String, Object> initialState() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("gameType", getType());
        state.put("boardSize", SIZE);
        state.put("board", copyBoard());
        state.put("nextUserId", getTurnUserId());
        state.put("redUserId", user1 == null ? 0 : user1.getUserId());
        state.put("blackUserId", user2 == null ? 0 : user2.getUserId());
        return state;
    }

    private int[][] copyBoard() {
        int[][] copy = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, SIZE);
        }
        return copy;
    }

    @Override
    public int getTurnUserId() {
        if (user1 == null || user2 == null) {
            return 0;
        }
        return (moveCount % 2 == 0) ? user1.getUserId() : user2.getUserId();
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public Integer getWinnerId() {
        return winnerId;
    }

    @Override
    public String getEndReason() {
        return endReason;
    }

    /** 判断刚刚落下的这一手是否形成五连 */
    private boolean checkWinner(int row, int col, int chess) {
        return countDirection(row, col, chess, 1, 0) + countDirection(row, col, chess, -1, 0) + 1 >= WIN_COUNT
                || countDirection(row, col, chess, 0, 1) + countDirection(row, col, chess, 0, -1) + 1 >= WIN_COUNT
                || countDirection(row, col, chess, 1, 1) + countDirection(row, col, chess, -1, -1) + 1 >= WIN_COUNT
                || countDirection(row, col, chess, 1, -1) + countDirection(row, col, chess, -1, 1) + 1 >= WIN_COUNT;
    }

    private int countDirection(int row, int col, int chess, int dr, int dc) {
        int count = 0;
        int r = row + dr;
        int c = col + dc;
        while (r >= 0 && r < SIZE && c >= 0 && c < SIZE && board[r][c] == chess) {
            count++;
            r += dr;
            c += dc;
        }
        return count;
    }
}
