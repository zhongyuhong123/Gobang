package com.yangshengzhou.gobang.game.games;

import com.fasterxml.jackson.databind.JsonNode;
import com.yangshengzhou.gobang.entity.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 中国象棋（完整规则）。
 *
 * 棋盘 10 行 x 9 列，row 0 在上方（黑方），row 9 在下方（红方）。
 * user1 执红先行，user2 执黑；棋子编码 = 阵营字符 + 兵种字符，例如 "rK"、"bP"。
 */
public class XiangqiGame implements Game {

    public static final int ROWS = 10;
    public static final int COLS = 9;

    private final String[][] board = new String[ROWS][COLS];
    private User user1;
    private User user2;
    private int turn = 0;                 // 0 = 红方, 1 = 黑方
    private Map<String, Object> lastMove;
    private Integer winnerId = null;
    private String endReason = null;
    private boolean finished = false;
    private int quietPlies = 0;

    @Override
    public String getType() {
        return "chinese-chess";
    }

    @Override
    public void init(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = null;
            }
        }
        String[] back = {"R", "N", "B", "A", "K", "A", "B", "N", "R"};
        for (int c = 0; c < COLS; c++) {
            board[0][c] = "b" + back[c];
            board[9][c] = "r" + back[c];
        }
        board[2][1] = "bC";
        board[2][7] = "bC";
        board[7][1] = "rC";
        board[7][7] = "rC";
        for (int c = 0; c < COLS; c += 2) {
            board[3][c] = "bP";
            board[6][c] = "rP";
        }
        turn = 0;
        finished = false;
        winnerId = null;
        endReason = null;
        quietPlies = 0;
        lastMove = null;
    }

    @Override
    public String applyMove(User player, JsonNode request) {
        if (finished) {
            return "本局已经结束";
        }
        int side = sideOf(player);
        if (side < 0) {
            return "你不在这局游戏里";
        }
        if (side != turn) {
            return "还没轮到你走棋";
        }

        int fr = request.path("fromRow").asInt(-1);
        int fc = request.path("fromCol").asInt(-1);
        int tr = request.path("toRow").asInt(-1);
        int tc = request.path("toCol").asInt(-1);
        if (!inside(fr, fc) || !inside(tr, tc)) {
            return "坐标越界";
        }
        String piece = board[fr][fc];
        if (piece == null) {
            return "起点没有棋子";
        }
        if (piece.charAt(0) != sideChar(side)) {
            return "请选择自己的棋子";
        }
        if (fr == tr && fc == tc) {
            return "起点和终点相同";
        }
        String captured = board[tr][tc];
        if (captured != null && captured.charAt(0) == sideChar(side)) {
            return "不能吃自己的棋子";
        }
        if (!canMove(piece, fr, fc, tr, tc)) {
            return "该棋子不能这样走";
        }

        // 试走，确认不能送将
        board[tr][tc] = piece;
        board[fr][fc] = null;
        if (!kingSafe(side)) {
            board[fr][fc] = piece;
            board[tr][tc] = captured;
            return "不能送将";
        }

        quietPlies = (captured == null) ? quietPlies + 1 : 0;
        Map<String, Object> lm = new LinkedHashMap<>();
        lm.put("fromRow", fr);
        lm.put("fromCol", fc);
        lm.put("toRow", tr);
        lm.put("toCol", tc);
        lm.put("captured", captured == null ? "" : captured);
        lastMove = lm;
        turn = 1 - turn;

        if (!hasLegalMove(turn)) {
            finished = true;
            winnerId = player.getUserId();
            endReason = inCheck(turn) ? "将死" : "困毙";
        } else if (quietPlies >= 120) {
            finished = true;
            winnerId = null;
            endReason = "60 回合内无吃子，和棋";
        }
        return null;
    }

    @Override
    public Map<String, Object> buildUpdate(User mover) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("message", "state");
        msg.put("gameType", getType());
        msg.put("board", snapshot());
        msg.put("nextUserId", getTurnUserId());
        msg.put("lastMove", lastMove);
        msg.put("winner", winnerId == null ? 0 : winnerId);
        return msg;
    }

    @Override
    public Map<String, Object> initialState() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("gameType", getType());
        state.put("board", snapshot());
        state.put("nextUserId", getTurnUserId());
        state.put("redUserId", user1 == null ? 0 : user1.getUserId());
        state.put("blackUserId", user2 == null ? 0 : user2.getUserId());
        return state;
    }

    @Override
    public int getTurnUserId() {
        if (user1 == null || user2 == null) {
            return 0;
        }
        return turn == 0 ? user1.getUserId() : user2.getUserId();
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

    // ================= 规则实现 =================

    private int sideOf(User player) {
        if (player == null || user1 == null || user2 == null) {
            return -1;
        }
        if (player.getUserId() == user1.getUserId()) {
            return 0;
        }
        if (player.getUserId() == user2.getUserId()) {
            return 1;
        }
        return -1;
    }

    private static char sideChar(int side) {
        return side == 0 ? 'r' : 'b';
    }

    private static boolean inside(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    private static boolean inPalace(char side, int r, int c) {
        if (c < 3 || c > 5) {
            return false;
        }
        return side == 'r' ? (r >= 7 && r <= 9) : (r >= 0 && r <= 2);
    }

    /** 该方自己的半场（象不能过河） */
    private static boolean ownHalf(char side, int r) {
        return side == 'r' ? r >= 5 : r <= 4;
    }

    private boolean canMove(String piece, int fr, int fc, int tr, int tc) {
        char side = piece.charAt(0);
        char type = piece.charAt(1);
        int dr = tr - fr;
        int dc = tc - fc;

        switch (type) {
            case 'K':
                return Math.abs(dr) + Math.abs(dc) == 1 && inPalace(side, tr, tc);
            case 'A':
                return Math.abs(dr) == 1 && Math.abs(dc) == 1 && inPalace(side, tr, tc);
            case 'B':
                if (Math.abs(dr) != 2 || Math.abs(dc) != 2 || !ownHalf(side, tr)) {
                    return false;
                }
                return board[fr + dr / 2][fc + dc / 2] == null;
            case 'N': {
                boolean shape = (Math.abs(dr) == 2 && Math.abs(dc) == 1)
                        || (Math.abs(dr) == 1 && Math.abs(dc) == 2);
                if (!shape) {
                    return false;
                }
                int lr = fr + (Math.abs(dr) == 2 ? dr / 2 : 0);
                int lc = fc + (Math.abs(dc) == 2 ? dc / 2 : 0);
                return board[lr][lc] == null;
            }
            case 'R':
                return straight(fr, fc, tr, tc) && countBetween(fr, fc, tr, tc) == 0;
            case 'C': {
                if (!straight(fr, fc, tr, tc)) {
                    return false;
                }
                int between = countBetween(fr, fc, tr, tc);
                return board[tr][tc] == null ? between == 0 : between == 1;
            }
            case 'P': {
                int forward = side == 'r' ? -1 : 1;
                if (dc == 0 && dr == forward) {
                    return true;
                }
                boolean crossed = side == 'r' ? fr <= 4 : fr >= 5;
                return crossed && dr == 0 && Math.abs(dc) == 1;
            }
            default:
                return false;
        }
    }

    private static boolean straight(int fr, int fc, int tr, int tc) {
        return fr == tr || fc == tc;
    }

    private int countBetween(int fr, int fc, int tr, int tc) {
        int count = 0;
        if (fr == tr) {
            int from = Math.min(fc, tc);
            int to = Math.max(fc, tc);
            for (int c = from + 1; c < to; c++) {
                if (board[fr][c] != null) {
                    count++;
                }
            }
        } else if (fc == tc) {
            int from = Math.min(fr, tr);
            int to = Math.max(fr, tr);
            for (int r = from + 1; r < to; r++) {
                if (board[r][fc] != null) {
                    count++;
                }
            }
        }
        return count;
    }

    private int[] findKing(char side) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                String p = board[r][c];
                if (p != null && p.charAt(0) == side && p.charAt(1) == 'K') {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    /** 己方帅/将是否安全（含将帅照面） */
    private boolean kingSafe(int side) {
        char me = sideChar(side);
        int[] king = findKing(me);
        if (king == null) {
            return false;
        }
        char enemy = me == 'r' ? 'b' : 'r';
        int[] enemyKing = findKing(enemy);
        // 将帅照面
        if (enemyKing != null && enemyKing[1] == king[1]) {
            boolean blocked = false;
            int from = Math.min(enemyKing[0], king[0]);
            int to = Math.max(enemyKing[0], king[0]);
            for (int r = from + 1; r < to; r++) {
                if (board[r][king[1]] != null) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) {
                return false;
            }
        }
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                String p = board[r][c];
                if (p == null || p.charAt(0) != enemy) {
                    continue;
                }
                // 敌方将/帅的贴身吃子也要算
                if (canMove(p, r, c, king[0], king[1])) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean inCheck(int side) {
        return !kingSafe(side);
    }

    private boolean hasLegalMove(int side) {
        char me = sideChar(side);
        for (int fr = 0; fr < ROWS; fr++) {
            for (int fc = 0; fc < COLS; fc++) {
                String p = board[fr][fc];
                if (p == null || p.charAt(0) != me) {
                    continue;
                }
                for (int tr = 0; tr < ROWS; tr++) {
                    for (int tc = 0; tc < COLS; tc++) {
                        if (fr == tr && fc == tc) {
                            continue;
                        }
                        String target = board[tr][tc];
                        if (target != null && target.charAt(0) == me) {
                            continue;
                        }
                        if (!canMove(p, fr, fc, tr, tc)) {
                            continue;
                        }
                        board[tr][tc] = p;
                        board[fr][fc] = null;
                        boolean ok = kingSafe(side);
                        board[fr][fc] = p;
                        board[tr][tc] = target;
                        if (ok) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private String[][] snapshot() {
        String[][] copy = new String[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, COLS);
        }
        return copy;
    }

    /** 调试/测试用：枚举某方所有合法走法 */
    public List<String> legalMoves(int side) {
        List<String> moves = new ArrayList<>();
        char me = sideChar(side);
        for (int fr = 0; fr < ROWS; fr++) {
            for (int fc = 0; fc < COLS; fc++) {
                String p = board[fr][fc];
                if (p == null || p.charAt(0) != me) {
                    continue;
                }
                for (int tr = 0; tr < ROWS; tr++) {
                    for (int tc = 0; tc < COLS; tc++) {
                        if (fr == tr && fc == tc) {
                            continue;
                        }
                        String target = board[tr][tc];
                        if (target != null && target.charAt(0) == me) {
                            continue;
                        }
                        if (!canMove(p, fr, fc, tr, tc)) {
                            continue;
                        }
                        board[tr][tc] = p;
                        board[fr][fc] = null;
                        boolean ok = kingSafe(side);
                        board[fr][fc] = p;
                        board[tr][tc] = target;
                        if (ok) {
                            moves.add(fr + "," + fc + "->" + tr + "," + tc);
                        }
                    }
                }
            }
        }
        return moves;
    }
}
