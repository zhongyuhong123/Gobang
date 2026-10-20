<template>
  <div class="jq-board" :class="{ 'jq-disabled': !canMove }">
    <div v-for="(row, r) in board" :key="r" class="jq-row">
      <div
        v-for="(cell, c) in row"
        :key="c"
        class="jq-cell"
        :class="cellClass(r, c)"
        @click="onClick(r, c)"
      >
        <span v-if="isCamp(r, c) && !cell" class="jq-mark">营</span>
        <span v-if="isHq(r, c) && !cell" class="jq-mark">大本营</span>
        <div v-if="cell" class="jq-piece" :class="cell[0] === 'r' ? 'jq-red' : 'jq-blue'">
          {{ label(cell) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue'

const NAMES = {
  flag: '军旗',
  mine: '地雷',
  bomb: '炸弹',
  9: '司令',
  8: '军长',
  7: '师长',
  6: '旅长',
  5: '团长',
  4: '营长',
  3: '连长',
  2: '排长',
  1: '工兵'
}

const CAMPS = new Set(['2,1', '2,3', '3,2', '4,1', '4,3', '7,1', '7,3', '8,2', '9,1', '9,3'])
const HQS = new Set(['0,1', '0,3', '11,1', '11,3'])
const RAIL_ROWS = new Set([1, 5, 6, 10])
const RAIL_COLS = new Set([0, 4])

const isRail = (r, c) => RAIL_ROWS.has(r) || (RAIL_COLS.has(c) && r >= 1 && r <= 10)

export default {
  name: 'JunqiBoard',
  props: {
    board: { type: Array, required: true },
    mySide: { type: String, default: 'r' },
    canMove: { type: Boolean, default: false },
    lastMove: { type: Object, default: null }
  },
  emits: ['move'],
  setup(props, { emit }) {
    const selected = ref(null)

    watch(() => props.board, () => { selected.value = null })

    const label = (cell) => NAMES[cell.substring(1)] || '?'
    const isCamp = (r, c) => CAMPS.has(`${r},${c}`)
    const isHq = (r, c) => HQS.has(`${r},${c}`)

    const cellClass = (r, c) => ({
      'jq-rail': isRail(r, c),
      'jq-camp': isCamp(r, c),
      'jq-hq': isHq(r, c),
      'jq-selected': !!selected.value && selected.value[0] === r && selected.value[1] === c,
      'jq-last': !!props.lastMove && props.lastMove.toRow === r && props.lastMove.toCol === c
    })

    const onClick = (r, c) => {
      if (!props.canMove) return
      const cell = props.board[r][c]
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

    return { label, isCamp, isHq, cellClass, onClick }
  }
}
</script>

<style scoped>
.jq-board {
  display: inline-block;
  padding: 14px;
  background: #d9e2d0;
  border: 3px solid #5d6b52;
  border-radius: 6px;
  box-shadow: inset 0 0 18px rgba(60, 80, 50, 0.25), 0 8px 24px rgba(0, 0, 0, 0.25);
  cursor: pointer;
  user-select: none;
}

.jq-row {
  display: flex;
}

.jq-cell {
  width: 74px;
  height: 46px;
  position: relative;
  border: 1px solid rgba(70, 90, 60, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.25);
}

.jq-rail {
  background: rgba(255, 255, 255, 0.55);
  border-color: rgba(50, 70, 45, 0.75);
}

.jq-camp {
  background: rgba(255, 235, 170, 0.75);
  border-radius: 6px;
}

.jq-hq {
  background: rgba(255, 205, 205, 0.75);
}

.jq-mark {
  position: absolute;
  font-size: 12px;
  color: rgba(60, 80, 50, 0.55);
  pointer-events: none;
}

.jq-piece {
  width: 62px;
  height: 38px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(180deg, #fffdf6, #efe6d2);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  border: 2px solid #b8a17c;
}

.jq-red {
  color: #c62828;
  border-color: #d8a3a3;
}

.jq-blue {
  color: #1a4fb3;
  border-color: #9fb4dc;
}

.jq-selected {
  background: rgba(24, 144, 255, 0.3);
}

.jq-last .jq-piece {
  box-shadow: 0 0 12px rgba(255, 193, 7, 0.95), 0 2px 6px rgba(0, 0, 0, 0.3);
}

.jq-disabled {
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .jq-cell { width: 54px; height: 34px; }
  .jq-piece { width: 46px; height: 28px; font-size: 13px; }
}
</style>
