<template>
  <div class="game-container">
    <GameNavbar 
      :title="gameTitle"
      :showBackButton="true"
    />
    
    <div class="game-info-bar glass-effect">
      <div class="game-info">
        <div class="info-item">
          <span class="label">我方:</span>
          <span class="value" :class="sideClass">{{ mySideText }}</span>
        </div>
        <div class="info-item">
          <span class="label">对手:</span>
          <span class="value">{{ opponentName || '等待对手…' }}</span>
        </div>
        <div class="info-item status">
          <span class="label">状态:</span>
          <span class="value">
            <span v-if="gameStatus === 'waiting'">等待游戏开始</span>
            <span v-else-if="gameStatus === 'ended'">游戏已结束</span>
            <span v-else-if="isMyTurn" class="my-turn">轮到你落子</span>
            <span v-else>等待对手落子…</span>
          </span>
        </div>
      </div>
    </div>

    <div class="game-content">
      <div class="game-board-wrapper">
        <div class="board-container">
          <div v-if="gameType === 'gobang'" class="board" :class="{ 'board-disabled': !canPlay }">
            <div v-if="gameStatus === 'ended'" class="result-overlay">
              <div class="result-text">{{ resultText }}</div>
              <div v-if="resultSub" class="result-sub">{{ resultSub }}</div>
            </div>
            <div class="board-lines">
              <div v-for="i in 14" :key="'h-' + i" class="horizontal-line" :style="{ top: `${i * (100 / 15)}%` }"></div>
              <div v-for="i in 14" :key="'v-' + i" class="vertical-line" :style="{ left: `${i * (100 / 15)}%` }"></div>
              
              <div class="star-point" style="top: 20%; left: 20%;"></div>
              <div class="star-point" style="top: 20%; left: 46.67%;"></div>
              <div class="star-point" style="top: 20%; left: 73.33%;"></div>
              <div class="star-point" style="top: 46.67%; left: 20%;"></div>
              <div class="star-point" style="top: 46.67%; left: 46.67%;"></div>
              <div class="star-point" style="top: 46.67%; left: 73.33%;"></div>
              <div class="star-point" style="top: 73.33%; left: 20%;"></div>
              <div class="star-point" style="top: 73.33%; left: 46.67%;"></div>
              <div class="star-point" style="top: 73.33%; left: 73.33%;"></div>
            </div>
            
            <div 
              v-for="(row, rowIndex) in board" 
              :key="rowIndex"
              class="board-row"
            >
              <div 
                v-for="(cell, colIndex) in row" 
                :key="colIndex"
                class="board-cell"
                @click.stop="handleBoardClick(rowIndex, colIndex)"
              >
                <div 
                  v-if="cell !== 0" 
                  class="chess-piece" 
                  :class="{ 
                    'black': cell === 1, 
                    'white': cell === 2,
                    'last-move': lastMoveRow === rowIndex && lastMoveCol === colIndex
                  }"
                ></div>
              </div>
            </div>
          </div>

          <XiangqiBoard
            v-else-if="gameType === 'chinese-chess'"
            :board="xqBoard"
            :my-side="mySide"
            :can-move="canPlay"
            :last-move="lastMove"
            @move="onBoardMove"
          />
          <JunqiBoard
            v-else
            :board="jqBoard"
            :my-side="mySide"
            :can-move="canPlay"
            :last-move="lastMove"
            @move="onBoardMove"
          />

          <div
            v-if="gameStatus === 'ended' && gameType !== 'gobang'"
            class="result-overlay"
          >
            <div class="result-text">{{ resultText }}</div>
            <div v-if="resultSub" class="result-sub">{{ resultSub }}</div>
          </div>
        </div>
        
        <div class="game-controls">
          <button class="btn primary control-button" @click="onPrimary">{{ primaryLabel }}</button>
          <button class="btn secondary control-button" @click="exitGame">退出游戏</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { wsManager } from '../api/index'
import GameNavbar from '@/components/GameNavbar.vue'
import XiangqiBoard from '@/components/games/XiangqiBoard.vue'
import JunqiBoard from '@/components/games/JunqiBoard.vue'

export default {
  name: 'GameView',
  components: {
    GameNavbar,
    XiangqiBoard,
    JunqiBoard
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const boardSize = 15

    const roomId = ref('')
    const board = ref([])
    const gameType = ref('gobang')
    const gameStatus = ref('waiting')   // waiting | playing | ended
    const turn = ref(1)                 // 五子棋：1 = 黑棋（先手），2 = 白棋
    const myUserId = ref(0)
    const opponentId = ref(0)
    const opponentName = ref('')
    const whiteUser = ref(0)            // 先手玩家的 userId
    const redUserId = ref(0)            // 象棋/军棋：红方 userId
    const blackUserId = ref(0)          // 象棋/军棋：黑方 userId
    const turnUserId = ref(0)           // 象棋/军棋：当前该走的 userId
    const xqBoard = ref([])
    const jqBoard = ref([])
    const lastMove = ref(null)
    const resultText = ref('')
    const resultSub = ref('')
    const lastMoveRow = ref(-1)
    const lastMoveCol = ref(-1)
    const websocket = ref(null)
    const sending = ref(false)

    const myColor = computed(() => (myUserId.value === whiteUser.value ? 1 : 2))
    const mySide = computed(() => (myUserId.value === redUserId.value ? 'r' : 'b'))
    const isMyTurn = computed(() => {
      if (gameStatus.value !== 'playing') {
        return false
      }
      if (gameType.value === 'gobang') {
        return turn.value === myColor.value
      }
      return turnUserId.value === myUserId.value
    })
    const canPlay = computed(() => gameStatus.value === 'playing' && isMyTurn.value && !sending.value)
    const primaryLabel = computed(() => (gameStatus.value === 'ended' ? '再来一局' : '认输'))
    const gameTitle = computed(() => {
      if (gameType.value === 'chinese-chess') return '中国象棋对战'
      if (gameType.value === 'military') return '军棋对战'
      return '五子棋对战'
    })
    const mySideText = computed(() => {
      if (gameType.value === 'gobang') {
        return myColor.value === 1 ? '黑棋（先手）' : '白棋'
      }
      return mySide.value === 'r' ? '红方' : '黑方'
    })
    const sideClass = computed(() => {
      if (gameType.value === 'gobang') {
        return myColor.value === 1 ? 'black-player' : 'white-player'
      }
      return mySide.value === 'r' ? 'red-player' : 'blue-player'
    })

    function createEmptyBoard() {
      const rows = []
      for (let i = 0; i < boardSize; i++) {
        const row = []
        for (let j = 0; j < boardSize; j++) {
          row.push(0)
        }
        rows.push(row)
      }
      return rows
    }

    const initBoard = () => {
      board.value = createEmptyBoard()
      lastMoveRow.value = -1
      lastMoveCol.value = -1
    }
    
    onMounted(() => {
      roomId.value = route.params.roomId || route.query.roomId || ''
      gameType.value = route.query.gameType || 'gobang'
      
      initBoard()
      gameStatus.value = 'waiting'
      turn.value = 1

      const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (!user.userId) {
        ElMessage.error('用户信息缺失，请重新登录')
        router.push('/login')
        return
      }
      myUserId.value = user.userId
      if (!roomId.value) {
        ElMessage.error('房间ID缺失，请重新匹配')
        router.push('/home')
        return
      }
      connectWebSocket()
    })
    
    const connectWebSocket = () => {
      if (websocket.value) {
        websocket.value.close()
      }

      // 进入房间前确保匹配连接已断开（后端会把仍在大厅的连接视为多开）
      if (wsManager.matchSocket) {
        wsManager.matchSocket.close()
        wsManager.matchSocket = null
      }

      websocket.value = wsManager.connectGameSocket(myUserId.value, roomId.value, {
        onOpen: () => {
          console.log('对局 WebSocket 已建立')
        },
        onMessage: handleGameMessage,
        onError: (error) => {
          console.error('WebSocket错误:', error)
          ElMessage.error('连接服务器失败')
        },
        onClose: () => {
          console.log('对局 WebSocket 已关闭')
          if (gameStatus.value === 'playing') {
            gameStatus.value = 'ended'
            resultText.value = '连接已断开'
            resultSub.value = '与服务器的连接中断，请重新匹配'
          }
        }
      })
    }

    const applyState = (state) => {
      if (!state) return
      if (state.gameType) gameType.value = state.gameType
      if (state.redUserId) redUserId.value = state.redUserId
      if (state.blackUserId) blackUserId.value = state.blackUserId
      if (state.nextUserId !== undefined) turnUserId.value = state.nextUserId
      if (state.lastMove) lastMove.value = state.lastMove
      if (state.board) {
        if (gameType.value === 'chinese-chess') {
          xqBoard.value = state.board
        } else if (gameType.value === 'military') {
          jqBoard.value = state.board
        } else {
          board.value = state.board
        }
      }
    }

    const handleGameMessage = (data) => {
      console.log('收到游戏消息:', data)

      if (data.message === 'gameReady') {
        opponentId.value = data.thatUserId
        opponentName.value = data.thatUsername || `玩家 #${data.thatUserId}`
        whiteUser.value = data.whiteUser
        if (data.thisUserId) {
          myUserId.value = data.thisUserId
        }
        if (data.gameType) {
          gameType.value = data.gameType
        }
        if (data.state) {
          applyState(data.state)
        }
        if (data.bothReady === false) {
          gameStatus.value = 'waiting'
          ElMessage.info('已进入房间，等待对手…')
        } else {
          gameStatus.value = 'playing'
          ElMessage.success(`对局开始，你执${mySideText.value}（${gameTitle.value}）`)
        }
        turn.value = 1
        sending.value = false
        return
      }

      if (data.message === 'putChess') {
        const color = data.userId === whiteUser.value ? 1 : 2
        if (data.row >= 0 && data.row < boardSize && data.col >= 0 && data.col < boardSize) {
          board.value[data.row][data.col] = color
          lastMoveRow.value = data.row
          lastMoveCol.value = data.col
        }
        turn.value = color === 1 ? 2 : 1
        sending.value = false
        return
      }

      if (data.message === 'state') {
        applyState(data)
        sending.value = false
        return
      }

      if (data.message === 'gameOver') {
        gameStatus.value = 'ended'
        sending.value = false
        if (data.winner === 0) {
          resultText.value = '平局'
        } else if (data.winner === myUserId.value) {
          resultText.value = '你赢了！'
        } else {
          resultText.value = '你输了'
        }
        resultSub.value = data.reason || ''
        ElMessage.success(resultText.value)
        return
      }

      if (data.message === 'error') {
        sending.value = false
        ElMessage.warning(data.reason || '操作失败')
        return
      }

      if (data.message === 'repeatConnection') {
        gameStatus.value = 'ended'
        resultText.value = '无法进入对局'
        resultSub.value = '你已经在另一个页面中开局了'
        ElMessage.error(resultSub.value)
        return
      }

      if (data.ok === false) {
        gameStatus.value = 'ended'
        resultText.value = data.reason || '无法进入对局'
        resultSub.value = data.message || ''
        ElMessage.error(resultText.value)
      }
    }
    
    const handleBoardClick = (row, col) => {
      if (gameStatus.value !== 'playing') {
        return
      }
      if (!isMyTurn.value) {
        ElMessage.warning('还没轮到你落子')
        return
      }
      if (sending.value) {
        return
      }
      if (board.value[row][col] !== 0) {
        ElMessage.warning('该位置已有棋子')
        return
      }

      const sent = wsManager.sendMessage(websocket.value, {
        message: 'putChess',
        userId: myUserId.value,
        row,
        col
      })
      if (sent) {
        // 等服务端回显后再允许下一步，避免连点导致重复落子
        sending.value = true
      } else {
        ElMessage.error('与服务器的连接已断开')
      }
    }

    const onBoardMove = (move) => {
      if (!canPlay.value || sending.value) {
        return
      }
      const sent = wsManager.sendMessage(websocket.value, {
        message: 'move',
        userId: myUserId.value,
        ...move
      })
      if (sent) {
        sending.value = true
      } else {
        ElMessage.error('与服务器的连接已断开')
      }
    }

    const goLobby = () => {
      wsManager.closeAll()
      router.push('/home')
    }

    // 对局中 = 认输；对局结束 = 回去重新匹配
    const onPrimary = async () => {
      if (gameStatus.value === 'ended') {
        goLobby()
        return
      }
      try {
        await ElMessageBox.confirm('确定要认输吗？', '提示', {
          confirmButtonText: '认输',
          cancelButtonText: '继续下棋',
          type: 'warning'
        })
      } catch (e) {
        return
      }
      wsManager.sendMessage(websocket.value, { message: 'surrender', userId: myUserId.value })
    }

    const exitGame = () => {
      // 关闭连接后，服务端会把仍在房间里的对手判为获胜
      wsManager.closeAll()
      router.push('/')
    }

    onUnmounted(() => {
      if (websocket.value) {
        websocket.value.close()
      }
      wsManager.closeAll()
    })

    return {
      board,
      gameType,
      gameTitle,
      gameStatus,
      myColor,
      mySide,
      mySideText,
      sideClass,
      xqBoard,
      jqBoard,
      lastMove,
      opponentName,
      isMyTurn,
      canPlay,
      primaryLabel,
      resultText,
      resultSub,
      lastMoveRow,
      lastMoveCol,

      handleBoardClick,
      onBoardMove,
      onPrimary,
      exitGame
    }
  }
}</script>

<style scoped>
.game-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-primary);
  position: relative;
  overflow: hidden;
}

.game-header {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  padding: 15px 20px;
  animation: slideDown 0.4s ease-out;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 80px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 15px;
  animation: fadeInLeft 0.5s ease-out;
}

.chess-logo {
  position: relative;
  width: 50px;
  height: 50px;
}

.chess-piece {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  position: absolute;
  box-shadow: var(--shadow-sm);
  animation: scaleIn 0.3s ease-out;
}

.chess-piece.black {
  background-color: var(--black-stone);
  background: radial-gradient(circle at 30% 30%, #333, #000);
  top: 0;
  left: 0;
  border: 2px solid var(--primary-color);
}

.chess-piece.white {
  background-color: var(--white-stone);
  background: radial-gradient(circle at 30% 30%, #fff, #ddd);
  bottom: 0;
  right: 0;
  border: 2px solid var(--success-color);
}

.game-title {
  font-size: 2rem;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.game-info {
  display: flex;
  gap: 30px;
  align-items: center;
  animation: fadeInRight 0.5s ease-out;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}

.info-item .value {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
}

.black-player {
  color: var(--black-stone);
  font-weight: 700;
}

.red-player {
  color: #c62828;
  font-weight: 700;
}

.blue-player {
  color: #1a4fb3;
  font-weight: 700;
}

.white-player {
  color: rgba(255, 255, 255, 0.95);
  font-weight: 700;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.5), 0 0 10px rgba(0, 0, 0, 0.2);
}

.info-item.status .value {
  color: var(--primary-color);
}

.game-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  animation: fadeIn 0.5s ease-out;
}

.game-board-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
}

.game-info-bar {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 2rem;
}

.game-info {
  display: flex;
  gap: 2rem;
  align-items: center;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 16px;
}

.info-item .label {
  font-weight: 600;
  color: var(--text-secondary);
}

.info-item .value {
  font-weight: 600;
  color: var(--text-primary);
}

.black-player {
  color: #333;
}

.white-player {
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.5), 0 0 10px rgba(0, 0, 0, 0.2);
}

.board-container {
  padding: 20px;
  position: relative;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--radius-md);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.board-container:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.board {
  --cell-size: 40px;
  width: calc(var(--cell-size) * 15);
  height: calc(var(--cell-size) * 15);
  position: relative;
  background-color: var(--board-color);
  border: 3px solid var(--board-border);
  border-radius: var(--radius-sm);
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.board:hover {
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.1), 0 0 20px rgba(24, 144, 255, 0.1);
}

.board-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.horizontal-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 1px;
  background-color: var(--board-line);
}

.vertical-line {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 1px;
  background-color: var(--board-line);
}

.star-point {
  position: absolute;
  width: 6px;
  height: 6px;
  background-color: var(--board-line);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
}

.board-row {
  display: flex;
  position: relative;
}

.board-cell {
  width: var(--cell-size);
  height: var(--cell-size);
  position: relative;
  z-index: 1;
  transition: background-color 0.2s ease;
}

.board-cell:hover {
  background-color: rgba(139, 69, 19, 0.1);
}

.board-cell .chess-piece {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: calc(var(--cell-size) * 0.85);
  height: calc(var(--cell-size) * 0.85);
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  cursor: default;
  transition: transform 0.1s ease;
}

.board-cell .chess-piece.black {
  background: radial-gradient(circle at 30% 30%, #555, #000);
  border: 1px solid #333;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.6);
}

.board-cell .chess-piece.white {
  background: radial-gradient(circle at 30% 30%, #fff, #ddd);
  border: 1px solid #bbb;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.board-cell .chess-piece.last-move {
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.6), 0 2px 12px rgba(0, 0, 0, 0.5);
  animation: pulse 1.5s infinite;
  transform: translate(-50%, -50%) scale(1.05);
}

.game-controls {
  display: flex;
  gap: 20px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  padding: 15px 20px;
  border-radius: var(--radius-md);
}

.control-button {
  padding: 12px 24px;
  font-size: 16px;
  min-width: 120px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.control-button:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.control-button:active {
  transform: translateY(0);
}

.my-turn {
  color: var(--success-color, #19be6b);
  font-weight: 700;
}

.board-disabled {
  cursor: not-allowed;
}

.board-disabled .board-cell:hover {
  background-color: transparent;
}

.result-overlay {
  position: absolute;
  inset: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  border-radius: var(--radius-sm);
}

.result-text {
  font-size: 2.2rem;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.6);
}

.result-sub {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.9);
}

@media (max-width: 768px) {
  .board {
    --cell-size: 35px;
  }
  
  .game-title {
    font-size: 1.5rem;
  }
  
  .game-info {
    gap: 20px;
  }
  
  .board-container {
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .board {
    --cell-size: 25px;
  }
  
  .header-content {
    padding: 0 10px;
  }
  
  .chess-logo {
    width: 40px;
    height: 40px;
  }
  
  .chess-piece {
    width: 24px;
    height: 24px;
  }
  
  .game-title {
    font-size: 1.25rem;
  }
  
  .game-info {
    gap: 10px;
  }
  
  .info-item {
    flex-direction: column;
    gap: 2px;
  }
  
  .info-item .label {
    font-size: 12px;
  }
  
  .info-item .value {
    font-size: 14px;
  }
  
  .board-container {
    padding: 10px;
  }
  
  .game-controls {
    gap: 10px;
  }
  
  .control-button {
    padding: 10px 16px;
    font-size: 14px;
    min-width: 100px;
  }
}
</style>