<template>
  <div class="xq-scroll">
    <div class="xq-board" :style="{ width: W + 'px', height: H + 'px' }">
      <svg class="xq-lines" :width="W" :height="H" :viewBox="`0 0 ${W} ${H}`">
        <!-- 外框 -->
        <rect
          :x="P - 7" :y="P - 7"
          :width="8 * S + 14" :height="9 * S + 14"
          rx="4" fill="none" stroke="#7a4b1e" stroke-width="3"
        />

        <!-- 10 条横线 -->
        <line
          v-for="r in 10" :key="'h' + r"
          :x1="P" :y1="y(r - 1)" :x2="P + 8 * S" :y2="y(r - 1)"
          stroke="#7a4b1e" stroke-width="1.2"
        />

        <!-- 9 条竖线：中间 7 条在河界断开 -->
        <template v-for="c in 9" :key="'v' + c">
          <line
            v-if="c === 1 || c === 9"
            :x1="x(c - 1)" :y1="P" :x2="x(c - 1)" :y2="P + 9 * S"
            stroke="#7a4b1e" stroke-width="1.2"
          />
          <template v-else>
            <line :x1="x(c - 1)" :y1="P" :x2="x(c - 1)" :y2="y(4)" stroke="#7a4b1e" stroke-width="1.2" />
            <line :x1="x(c - 1)" :y1="y(5)" :x2="x(c - 1)" :y2="P + 9 * S" stroke="#7a4b1e" stroke-width="1.2" />
          </template>
        </template>

        <!-- 九宫斜线 -->
        <line
          v-for="(d, i) in diagonals" :key="'d' + i"
          :x1="d.x1" :y1="d.y1" :x2="d.x2" :y2="d.y2"
          stroke="#7a4b1e" stroke-width="1.2"
        />

        <!-- 河界 -->
        <text class="xq-river-text" :x="x(2)" :y="y(4.5)">楚 河</text>
        <text class="xq-river-text" :x="x(6)" :y="y(4.5)">汉 界</text>
      </svg>

      <!-- 点击层：90 个交叉点 -->
      <div
        v-for="pt in points" :key="'p' + pt.r + '-' + pt.c"
        class="xq-hit"
        :style="{ left: x(pt.c) - S / 2 + 'px', top: y(pt.r) - S / 2 + 'px', width: S + 'px', height: S + 'px' }"
        @click="onClick(pt.r, pt.c)"
      >
        <span v-if="!board[pt.r] || !board[pt.r][pt.c]" class="xq-dot"></span>
      </div>

      <!-- 棋子 -->
      <div
        v-for="p in pieces" :key="'c' + p.key"
        class="xq-piece"
        :class="[
          p.side === 'r' ? 'xq-red' : 'xq-black',
          { 'xq-selected': isSelected(p.r, p.c) }
        ]"
        :style="pieceStyle(p.r, p.c)"
      >
        {{ p.label }}
      </div>

      <!-- 最后一手标记 -->
      <div
        v-if="lastMove"
        class="xq-last"
        :style="ringStyle(lastMove.toRow, lastMove.toCol)"
      ></div>
    </div>
  </div>
</template>

<script>
import { computed, ref, watch } from 'vue'

const RED = { K: '帅', A: '仕', B: '相', N: '马', R: '车', C: '炮', P: '兵' }
const BLACK = { K: '将', A: '士', B: '象', N: '马', R: '车', C: '炮', P: '卒' }

export default {
  name: 'XiangqiBoard',
  props: {
    board: { type: Array, default: () => [] },
    mySide: { type: String, default: 'r' },
    canMove: { type: Boolean, default: false },
    lastMove: { type: Object, default: null }
  },
  emits: ['move'],
  setup(props, { emit }) {
    const S = 50          // 交叉点间距
    const P = 34          // 边距（给棋子留位置）
    const W = 8 * S + 2 * P
    const H = 9 * S + 2 * P
    const PIECE = Math.round(S * 0.9)

    const x = (c) => P + c * S
    const y = (r) => P + r * S

    const selected = ref(null)
    watch(() => props.board, () => { selected.value = null })

    const points = computed(() => {
      const list = []
      for (let r = 0; r < 10; r++) {
        for (let c = 0; c < 9; c++) {
          list.push({ r, c })
        }
      }
      return list
    })

    const pieces = computed(() => {
      const list = []
      const board = props.board || []
      for (let r = 0; r < board.length; r++) {
        for (let c = 0; c < (board[r] || []).length; c++) {
          const cell = board[r][c]
          if (!cell) continue
          const map = cell[0] === 'r' ? RED : BLACK
          list.push({
            r,
            c,
            key: r + '-' + c,
            side: cell[0],
            label: map[cell[1]] || '?'
          })
        }
      }
      return list
    })

    const diagonals = [
      [3, 0, 5, 2], [5, 0, 3, 2],
      [3, 7, 5, 9], [5, 7, 3, 9]
    ].map(([c1, r1, c2, r2]) => ({ x1: x(c1), y1: y(r1), x2: x(c2), y2: y(r2) }))

    const pieceStyle = (r, c) => ({
      left: x(c) - PIECE / 2 + 'px',
      top: y(r) - PIECE / 2 + 'px',
      width: PIECE + 'px',
      height: PIECE + 'px',
      fontSize: Math.round(PIECE * 0.5) + 'px'
    })

    const ringStyle = (r, c) => {
      const size = Math.round(PIECE * 1.06)
      return {
        left: x(c) - size / 2 + 'px',
        top: y(r) - size / 2 + 'px',
        width: size + 'px',
        height: size + 'px'
      }
    }

    const isSelected = (r, c) => !!selected.value && selected.value[0] === r && selected.value[1] === c

    const onClick = (r, c) => {
      if (!props.canMove) return
      const row = props.board[r] || []
      const cell = row[c]
      if (selected.value) {
        if (selected.value[0] === r && selected.value[1] === c) {
          selected.value = null
          return
        }
        if (cell && cell[0] === props.mySide) {
          selected.value = [r, c]
          return
        }
        emit('move', {
          fromRow: selected.value[0],
          fromCol: selected.value[1],
          toRow: r,
          toCol: c
        })
        selected.value = null
        return
      }
      if (cell && cell[0] === props.mySide) {
        selected.value = [r, c]
      }
    }

    return {
      S, P, W, H,
      x, y,
      points,
      pieces,
      diagonals,
      pieceStyle,
      ringStyle,
      isSelected,
      onClick
    }
  }
}
</script>

<style scoped>
.xq-scroll {
  max-width: 100%;
  overflow-x: auto;
  padding: 4px;
}

.xq-board {
  position: relative;
  border-radius: 10px;
  background:
    radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.45), transparent 55%),
    linear-gradient(160deg, #f2d9a8 0%, #e8c88e 45%, #dcb877 100%);
  box-shadow:
    inset 0 0 26px rgba(140, 90, 35, 0.35),
    0 10px 26px rgba(0, 0, 0, 0.28);
  user-select: none;
}

.xq-lines {
  position: absolute;
  left: 0;
  top: 0;
  pointer-events: none;
}

.xq-river-text {
  fill: rgba(122, 75, 30, 0.62);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 10px;
  text-anchor: middle;
  dominant-baseline: middle;
}

.xq-hit {
  position: absolute;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
}

.xq-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(122, 75, 30, 0);
  transition: background 0.15s ease;
}

.xq-hit:hover .xq-dot {
  background: rgba(122, 75, 30, 0.45);
}

.xq-piece {
  position: absolute;
  z-index: 2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  letter-spacing: 0;
  pointer-events: none;
  background: radial-gradient(circle at 32% 28%, #fffdf3 0%, #f3e6c8 55%, #dcc79b 100%);
  border: 2px solid #b9a077;
  box-shadow:
    0 2px 5px rgba(60, 35, 10, 0.45),
    inset 0 -2px 4px rgba(150, 110, 60, 0.25);
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.xq-red {
  color: #b52020;
  border-color: #d8a3a3;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.7);
}

.xq-black {
  color: #1e1e1e;
  border-color: #a9a9a9;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.7);
}

.xq-selected {
  transform: scale(1.08);
  box-shadow:
    0 0 0 3px rgba(24, 144, 255, 0.85),
    0 2px 8px rgba(0, 0, 0, 0.45);
}

.xq-last {
  position: absolute;
  z-index: 3;
  border-radius: 50%;
  border: 2px dashed #f0a500;
  pointer-events: none;
  box-sizing: border-box;
  animation: xq-pulse 1.6s ease-in-out infinite;
}

@keyframes xq-pulse {
  0%, 100% { opacity: 0.45; }
  50% { opacity: 1; }
}
</style>
