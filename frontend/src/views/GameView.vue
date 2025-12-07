<template>
  <div class="game-container">
    <div class="bg-pattern"></div>
    <header class="game-header">
      <div class="header-content">
        <div class="logo-area">
          <div class="chess-logo">
            <div class="chess-piece black"></div>
            <div class="chess-piece white"></div>
          </div>
          <h2 class="game-title">Come 下棋</h2>
        </div>
        <div class="game-info">
          <div class="info-item">
            <span class="label">当前玩家：</span>
            <span class="value" :class="currentPlayer === 1 ? 'black-player' : 'white-player'">
              {{ currentPlayer === 1 ? '黑方' : '白方' }}
            </span>
          </div>
          <div class="info-item status">
            <span class="label">状态：</span>
            <span class="value">{{ gameStatus }}</span>
          </div>
        </div>
      </div>
    </header>
    <main class="game-content">
      <div class="game-board-wrapper">
        <div class="board-container">
          <div class="board" @click="handleBoardClick">
            <!-- 棋盘线 -->
            <div class="board-lines">
              <!-- 横线 -->
              <div 
                v-for="i in boardSize" 
                :key="'h-' + i" 
                class="horizontal-line"
                :style="{ top: `${(i - 1) * cellSize}px` }"
              ></div>
              <!-- 竖线 -->
              <div 
                v-for="i in boardSize" 
                :key="'v-' + i" 
                class="vertical-line"
                :style="{ left: `${(i - 1) * cellSize}px` }"
              ></div>
              <!-- 星位 -->
              <div class="star-point" style="top: calc(3 * var(--cell-size)); left: calc(3 * var(--cell-size));"></div>
              <div class="star-point" style="top: calc(7 * var(--cell-size)); left: calc(7 * var(--cell-size));"></div>
              <div class="star-point" style="top: calc(11 * var(--cell-size)); left: calc(11 * var(--cell-size));"></div>
              <div class="star-point" style="top: calc(3 * var(--cell-size)); left: calc(11 * var(--cell-size));"></div>
              <div class="star-point" style="top: calc(11 * var(--cell-size)); left: calc(3 * var(--cell-size));"></div>
            </div>
            <!-- 棋子 -->
            <div 
              v-for="(row, rowIndex) in boardSize" 
              :key="'row-' + rowIndex" 
              class="board-row"
            >
              <div 
                v-for="(col, colIndex) in boardSize" 
                :key="'col-' + colIndex"
                class="board-cell"
                :data-row="rowIndex"
                :data-col="colIndex"
              >
                <div 
                  v-if="board[rowIndex][colIndex] !== 0"
                  class="chess-piece"
                  :class="{
                    'black': board[rowIndex][colIndex] === 1,
                    'white': board[rowIndex][colIndex] === 2,
                    'last-move': rowIndex === lastMoveRow && colIndex === lastMoveCol
                  }"
                ></div>
              </div>
            </div>
          </div>
        </div>
        <div class="game-controls">
          <el-button type="primary" @click="exitGame" class="game-button control-button">退出游戏</el-button>
          <el-button type="success" @click="restartGame" class="game-button control-button">重新开始</el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'GameView',
  setup() {
    const router = useRouter()
    const boardSize = 15
    const cellSize = 40 // 棋子大小
    const board = ref([])
    const currentPlayer = ref(1) // 1: 黑方, 2: 白方
    const gameStatus = ref('游戏进行中')
    const websocket = ref(null)
    const roomId = ref(1) // 默认房间ID，实际应该从路由参数或匹配结果获取
    const lastMoveRow = ref(-1)
    const lastMoveCol = ref(-1)

    // 计算单元格大小
    const cellSizeStyle = computed(() => {
      return `--cell-size: ${cellSize}px`
    })

    // 初始化棋盘
    const initBoard = () => {
      board.value = Array(boardSize).fill(0).map(() => Array(boardSize).fill(0))
      lastMoveRow.value = -1
      lastMoveCol.value = -1
    }

    // 建立WebSocket连接
    const connectWebSocket = () => {
      try {
        const user = JSON.parse(localStorage.getItem('user'))
        if (!user) {
          router.push('/')
          return
        }

        // WebSocket连接URL，实际应该根据后端配置调整
        const wsUrl = `ws://localhost:8080/game?userId=${user.userId}&roomId=${roomId.value}`
        websocket.value = new WebSocket(wsUrl)

        websocket.value.onopen = () => {
          console.log('WebSocket连接已建立')
          ElMessage.success('已连接到游戏服务器')
        }

        websocket.value.onmessage = (event) => {
          const data = JSON.parse(event.data)
          handleGameMessage(data)
        }

        websocket.value.onerror = (error) => {
          console.error('WebSocket错误:', error)
          ElMessage.error('与服务器连接失败')
        }

        websocket.value.onclose = () => {
          console.log('WebSocket连接已关闭')
        }
      } catch (error) {
        console.error('建立WebSocket连接失败:', error)
        ElMessage.error('连接游戏服务器失败')
      }
    }

    // 处理游戏消息
    const handleGameMessage = (data) => {
      switch (data.type) {
        case 'GAME_START':
          gameStatus.value = '游戏开始'
          currentPlayer.value = data.currentPlayer
          ElMessage.info('游戏开始！')
          break
        case 'MOVE':
          board.value[data.row][data.col] = data.player
          lastMoveRow.value = data.row
          lastMoveCol.value = data.col
          currentPlayer.value = data.nextPlayer
          break
        case 'GAME_OVER':
          gameStatus.value = data.winner ? (data.winner === 1 ? '黑方获胜！' : '白方获胜！') : '平局'
          ElMessage.success(gameStatus.value)
          break
        case 'ERROR':
          ElMessage.error(data.message)
          break
        default:
          console.log('未知消息类型:', data.type)
      }
    }

    // 处理棋盘点击
    const handleBoardClick = (event) => {
      if (gameStatus.value !== '游戏进行中') return

      const boardElement = event.currentTarget
      const rect = boardElement.getBoundingClientRect()
      const x = event.clientX - rect.left
      const y = event.clientY - rect.top
      
      const col = Math.floor(x / cellSize)
      const row = Math.floor(y / cellSize)

      // 检查位置是否合法
      if (row >= 0 && row < boardSize && col >= 0 && col < boardSize && board.value[row][col] === 0) {
        // 发送落子请求
        if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
          websocket.value.send(JSON.stringify({
            type: 'MOVE',
            row,
            col,
            player: currentPlayer.value
          }))
        } else {
          ElMessage.error('未连接到游戏服务器')
        }
      }
    }

    // 退出游戏
    const exitGame = () => {
      if (websocket.value) {
        websocket.value.close()
      }
      router.push('/home')
    }

    // 重新开始
    const restartGame = () => {
      initBoard()
      currentPlayer.value = 1
      gameStatus.value = '游戏进行中'
      if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
        websocket.value.send(JSON.stringify({ type: 'RESTART' }))
      }
      ElMessage.info('游戏已重新开始')
    }

    onMounted(() => {
      initBoard()
      connectWebSocket()
    })

    onUnmounted(() => {
      if (websocket.value) {
        websocket.value.close()
      }
    })

    return {
      boardSize,
      cellSize,
      board,
      currentPlayer,
      gameStatus,
      lastMoveRow,
      lastMoveCol,
      cellSizeStyle,
      handleBoardClick,
      exitGame,
      restartGame
    }
  }
}
</script>

<style scoped>
.game-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
  position: relative;
  overflow: hidden;
}

.game-header {
  background: rgba(30, 41, 59, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-lg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
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
  box-shadow: var(--shadow-md);
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
  font-weight: bold;
  color: var(--text-primary);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.game-info {
  display: flex;
  gap: 30px;
  align-items: center;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  color: var(--text-secondary);
  font-size: 14px;
}

.info-item .value {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: bold;
}

.black-player {
  color: #000;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.white-player {
  color: #fff;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
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
}

.game-board-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
}

.board-container {
  padding: 20px;
  background: rgba(30, 41, 59, 0.8);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  backdrop-filter: blur(10px);
}

.board {
  width: calc(var(--cell-size) * 15);
  height: calc(var(--cell-size) * 15);
  position: relative;
  background-color: var(--board-color);
  border: 3px solid var(--board-border);
  border-radius: 5px;
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.board:hover {
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.3), 0 0 30px rgba(24, 144, 255, 0.2);
}

/* 棋盘线 */
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

/* 星位 */
.star-point {
  position: absolute;
  width: 6px;
  height: 6px;
  background-color: var(--board-line);
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

/* 棋盘格子 */
.board-row {
  display: flex;
  position: relative;
}

.board-cell {
  width: var(--cell-size);
  height: var(--cell-size);
  position: relative;
  z-index: 1;
}

.board-cell:hover {
  background-color: rgba(139, 69, 19, 0.1);
}

/* 棋子样式 */
.board-cell .chess-piece {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: calc(var(--cell-size) * 0.85);
  height: calc(var(--cell-size) * 0.85);
  border-radius: 50%;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.5);
  cursor: default;
}

.board-cell .chess-piece.black {
  background: radial-gradient(circle at 30% 30%, #555, #000);
  border: 1px solid #333;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.8);
}

.board-cell .chess-piece.white {
  background: radial-gradient(circle at 30% 30%, #fff, #ddd);
  border: 1px solid #bbb;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.3);
}

/* 最后一步高亮 */
.board-cell .chess-piece.last-move {
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.8), 0 2px 15px rgba(0, 0, 0, 0.5);
  animation: pulse 1s infinite;
}

.game-controls {
  display: flex;
  gap: 20px;
}

.control-button {
  padding: 12px 24px !important;
  font-size: 16px !important;
  min-width: 120px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :root {
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
  :root {
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
    padding: 10px 16px !important;
    font-size: 14px !important;
    min-width: 100px;
  }
}
</style>
