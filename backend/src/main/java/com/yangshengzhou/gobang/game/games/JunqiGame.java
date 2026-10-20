package com.yangshengzhou.gobang.game.games;

import com.fasterxml.jackson.databind.JsonNode;
import com.yangshengzhou.gobang.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 军棋（陆战棋，明棋版：双方棋子全部可见）。
 *
 * 棋盘 12 行 x 5 列：user1 执红在下方（row 6-11），user2 执蓝在上方（row 0-5），红先。
 * 棋子编码 "r9"、"bflag" 等，兵种：flag 军旗、mine 地雷、bomb 炸弹、9 司令 … 1 工兵。
 *
 * 规则要点：
 *  - 公路相邻一格；铁路可直线滑行任意格（不能越过棋子）；工兵在铁路上可以拐弯。
 *  - 行营内的棋子不能被攻击；大本营里的棋子不能再移动；军旗、地雷不能移动。
 *  - 战斗：大吃小；同级同归于尽；炸弹与任何棋子同归于尽；地雷只有工兵能排掉。
 *  - 军旗被吃 / 对方无子可动 判负；100 手无战斗判和。
 */
public class JunqiGame implements Game {

    public static final int ROWS = 12;
    public static final int COLS = 5;
    private static final int DRAW_PLIES = 100;

    private final String[][] board = new String[ROWS][COLS];
    private User user1;   // 红（下）
    private User user2;   // 蓝（上）
    private int turn = 0; // 0 = 红, 1 = 蓝
    private int plies = 0;
    private int quietPlies = 0;
    private Map<String, Object> lastMove;
    private Integer winnerId = null;
    private String endReason = null;
    private boolean finished = false;

    private static final Set<Integer> RAIL_ROWS = new LinkedHashSet<>(Arrays.asList(1, 5, 6, 10));
    private static final Set<Integer> RAIL_COLS = new LinkedHashSet<>(Arrays.asList(0, 4));
    private static final Set<String> CAMPS = new LinkedHashSet<>(Arrays.asList(
            "2,1", "2,3", "3,2", "4,1", "4,3",
            "7,1", "7,3", "8,2", "9,1", "9,3"));
    private static final Set<String> HQS = new LinkedHashSet<>(Arrays.asList(
            "0,1", "0,3", "11,1", "11,3"));

    @Override
    public String getType() {
        return "military";
    }

    @Override
    public void init(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        for (int r = 0; r < ROWS; r++) {
            Arrays.fill(board[r], null);
        }
        deploy("b", 0);
        deploy("r", 1);
        turn = 0;
        plies = 0;
        quietPlies = 0;
        finished = false;
        winnerId = null;
        endReason = null;
        lastMove = null;
    }

    // ================= 布阵 =================

    private void deploy(String side, int sideIndex) {
        List<String> pieces = new ArrayList<>();
        pieces.add("flag");
        pieces.addAll(Collections.nCopies(3, "mine"));
        pieces.addAll(Collections.nCopies(2, "bomb"));
        pieces.add("9");
        pieces.add("8");
        pieces.addAll(Collections.nCopies(2, "7"));
        pieces.addAll(Collections.nCopies(2, "6"));
        pieces.addAll(Collections.nCopies(2, "5"));
        pieces.addAll(Collections.nCopies(2, "4"));
        pieces.addAll(Collections.nCopies(3, "3"));
        pieces.addAll(Collections.nCopies(3, "2"));
        pieces.addAll(Collections.nCopies(3, "1"));

        Random random = new Random();
        int rowStart = sideIndex == 0 ? 0 : 6;
        int frontRow = sideIndex == 0 ? 5 : 6;
        int backRow1 = sideIndex == 0 ? 0 : 10;
        int backRow2 = sideIndex == 0 ? 1 : 11;
        int hqRow = sideIndex == 0 ? 0 : 11;

        List<String> ownCells = new ArrayList<>();
        for (int r = rowStart; r < rowStart + 6; r++) {
            for (int c = 0; c < COLS; c++) {
                ownCells.add(r + "," + c);
            }
        }

        // 军旗放在两个大本营之一
        List<String> hqCells = new ArrayList<>(Arrays.asList(hqRow + ",1", hqRow + ",3"));
        Collections.shuffle(hqCells, random);
        String flagCell = hqCells.get(0);
        place(side, flagCell, "flag");

        // 地雷放在己方最后两排
        List<String> backCells = new ArrayList<>();
        for (int c = 0; c < COLS; c++) {
            backCells.add(backRow1 + "," + c);
            backCells.add(backRow2 + "," + c);
        }
        backCells.remove(flagCell);
        Collections.shuffle(backCells, random);
        int mineCount = 3;
        for (int i = 0; i < mineCount && i < backCells.size(); i++) {
            place(side, backCells.get(i), "mine");
        }

        // 炸弹不能摆在第一排
        List<String> rest = new ArrayList<>(ownCells);
        rest.removeIf(cell -> {
            int[] rc = parse(cell);
            return board[rc[0]][rc[1]] != null || rc[0] == frontRow;
        });
        Collections.shuffle(rest, random);

        int index = 0;
        for (String piece : pieces) {
            if ("flag".equals(piece) || "mine".equals(piece)) {
                continue;
            }
            while (index < rest.size() && board[parse(rest.get(index))[0]][parse(rest.get(index))[1]] != null) {
                index++;
            }
            if (index >= rest.size()) {
                break;
            }
            place(side, rest.get(index), piece);
            index++;
        }

        // 极端情况下还有剩余格子，兜底补满
        for (String cell : rest) {
            int[] rc = parse(cell);
            if (board[rc[0]][rc[1]] == null) {
                place(side, cell, "1");
            }
        }
    }

    private void place(String side, String cell, String type) {
        int[] rc = parse(cell);
        board[rc[0]][rc[1]] = side + type;
    }

    private static int[] parse(String cell) {
        String[] parts = cell.split(",");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    // ================= 走子 =================

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
        if (!canMove(fr, fc, tr, tc)) {
            return "该棋子不能这样走";
        }

        String target = board[tr][tc];
        String combat = "move";
        boolean moverDies = false;
        boolean targetDies = false;
        boolean flagCaptured = false;

        if (target != null) {
            String attackerType = piece.substring(1);
            String defenderType = target.substring(1);

            if ("flag".equals(defenderType)) {
                flagCaptured = true;
                targetDies = true;
            } else if ("bomb".equals(attackerType) || "bomb".equals(defenderType)) {
                moverDies = true;
                targetDies = true;
                combat = "bomb";
            } else if ("mine".equals(defenderType)) {
                if ("1".equals(attackerType)) {
                    targetDies = true;
                    combat = "mine-swept";
                } else {
                    moverDies = true;
                    combat = "mine";
                }
            } else {
                int a = rank(attackerType);
                int d = rank(defenderType);
                if (a > d) {
                    targetDies = true;
                    combat = "win";
                } else if (a < d) {
                    moverDies = true;
                    combat = "lose";
                } else {
                    moverDies = true;
                    targetDies = true;
                    combat = "draw";
                }
            }
        }

        // 落子/结算
        board[fr][fc] = null;
        if (moverDies) {
            // 攻击方阵亡，目标若也阵亡则清空
            if (targetDies) {
                board[tr][tc] = null;
            }
        } else {
            board[tr][tc] = piece;
        }

        plies++;
        quietPlies = (target == null) ? quietPlies + 1 : 0;
        Map<String, Object> lm = new LinkedHashMap<>();
        lm.put("fromRow", fr);
        lm.put("fromCol", fc);
        lm.put("toRow", tr);
        lm.put("toCol", tc);
        lm.put("combat", combat);
        lastMove = lm;

        turn = 1 - turn;

        if (flagCaptured) {
            finished = true;
            winnerId = player.getUserId();
            endReason = "夺得军旗";
        } else if (!hasAnyMove(turn)) {
            finished = true;
            winnerId = player.getUserId();
            endReason = "对方无子可动";
        } else if (quietPlies >= DRAW_PLIES) {
            finished = true;
            winnerId = null;
            endReason = "长时间无战斗，和棋";
        }
        return null;
    }

    private static int rank(String type) {
        try {
            return Integer.parseInt(type);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

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

    private static boolean isRail(int r, int c) {
        return RAIL_ROWS.contains(r) || (RAIL_COLS.contains(c) && r >= 1 && r <= 10);
    }

    private static boolean isCamp(int r, int c) {
        return CAMPS.contains(r + "," + c);
    }

    private static boolean isHq(int r, int c) {
        return HQS.contains(r + "," + c);
    }

    private boolean canMove(int fr, int fc, int tr, int tc) {
        if (fr == tr && fc == tc) {
            return false;
        }
        String piece = board[fr][fc];
        if (piece == null) {
            return false;
        }
        String type = piece.substring(1);
        if ("flag".equals(type) || "mine".equals(type)) {
            return false;
        }
        // 大本营里的棋子不能再动
        if (isHq(fr, fc)) {
            return false;
        }
        String target = board[tr][tc];
        if (target != null) {
            if (target.charAt(0) == piece.charAt(0)) {
                return false;
            }
            // 行营里的棋子不能被吃
            if (isCamp(tr, tc)) {
                return false;
            }
        } else if (isCamp(tr, tc) && target != null) {
            return false;
        }

        // 铁路移动
        if (isRail(fr, fc) && isRail(tr, tc)) {
            if ("1".equals(type)) {
                if (engineerReach(fr, fc, tr, tc)) {
                    return true;
                }
            } else if (railStraightReach(fr, fc, tr, tc)) {
                return true;
            }
        }

        // 公路相邻
        return Math.abs(fr - tr) + Math.abs(fc - tc) == 1;
    }

    /** 非工兵：沿铁路直线滑行，中间不能有子 */
    private boolean railStraightReach(int fr, int fc, int tr, int tc) {
        if (fr == tr) {
            int step = Integer.signum(tc - fc);
            for (int c = fc + step; c != tc; c += step) {
                if (board[fr][c] != null) {
                    return false;
                }
            }
            return true;
        }
        if (fc == tc) {
            int step = Integer.signum(tr - fr);
            for (int r = fr + step; r != tr; r += step) {
                if (board[r][fc] != null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /** 工兵：沿铁路线 BFS，可拐弯，不能穿过棋子 */
    private boolean engineerReach(int fr, int fc, int tr, int tc) {
        boolean[][] seen = new boolean[ROWS][COLS];
        List<int[]> queue = new ArrayList<>();
        queue.add(new int[]{fr, fc});
        seen[fr][fc] = true;
        int head = 0;
        while (head < queue.size()) {
            int[] cur = queue.get(head++);
            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] d : dirs) {
                int r = cur[0];
                int c = cur[1];
                while (true) {
                    r += d[0];
                    c += d[1];
                    if (!inside(r, c) || !isRail(r, c)) {
                        break;
                    }
                    if (r == tr && c == tc) {
                        return true;
                    }
                    if (board[r][c] != null) {
                        break;
                    }
                    if (!seen[r][c]) {
                        seen[r][c] = true;
                        queue.add(new int[]{r, c});
                    }
                }
            }
        }
        return false;
    }

    private boolean hasAnyMove(int side) {
        char me = sideChar(side);
        for (int fr = 0; fr < ROWS; fr++) {
            for (int fc = 0; fc < COLS; fc++) {
                String p = board[fr][fc];
                if (p == null || p.charAt(0) != me) {
                    continue;
                }
                for (int tr = 0; tr < ROWS; tr++) {
                    for (int tc = 0; tc < COLS; tc++) {
                        if (canMove(fr, fc, tr, tc)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // ================= 状态 / 接口 =================

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

    private String[][] snapshot() {
        String[][] copy = new String[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, COLS);
        }
        return copy;
    }

    /** 测试用：列出某方所有可走的一步 */
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
                        if (canMove(fr, fc, tr, tc)) {
                            moves.add(fr + "," + fc + "->" + tr + "," + tc);
                        }
                    }
                }
            }
        }
        return moves;
    }

    public int getPlies() {
        return plies;
    }
}
