<template>
  <div class="game-container">
    <div class="game-header">
      <h1>五子棋对战</h1>
      <div class="game-info">
        <div class="player-info">
          <div class="player" :class="{ active: currentPlayer === 1 }">
            <div class="player-avatar black"></div>
            <span>黑方: {{ user1?.username || '等待玩家' }}</span>
          </div>
          <div class="vs">VS</div>
          <div class="player" :class="{ active: currentPlayer === 2 }">
            <div class="player-avatar white"></div>
            <span>白方: {{ user2?.username || '等待玩家' }}</span>
          </div>
        </div>
        <div class="game-status">
          <span v-if="gameStatus === 'waiting'">等待游戏开始</span>
          <span v-else-if="gameStatus === 'playing'">游戏进行中</span>
          <span v-else-if="gameStatus === 'ended'">游戏已结束</span>
        </div>
      </div>
    </div>

    <div class="game-board-container">
      <div class="game-board" @click="handleBoardClick">
        <!-- 棋盘格子 -->
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
            <!-- 棋子 -->
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
    </div>

    <div class="game-controls">
      <button class="btn btn-restart" @click="restartGame">重新开始</button>
      <button class="btn btn-exit" @click="exitGame">退出游戏</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import wsManager from '@/utils/websocket'

export default {
  name: 'GameView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const boardSize = 15
    const cellSize = 40 // 棋子大小
    const board = ref([])
    const currentPlayer = ref(1) // 1: 黑方, 2: 白方
    const gameStatus = ref('游戏进行中')
    const websocket = ref(null)
    const roomId = ref(1) // 默认房间ID，实际应该从路由参数或匹配结果获取
    const lastMoveRow = ref(-1)
    const lastMoveCol = ref(-1)
    const user1 = ref(null) // 玩家1信息
    const user2 = ref(null) // 玩家2信息

    // 计算单元格大小
    const cellSizeStyle = computed(() => {
      return `--cell-size: ${cellSize}px`
    })

    // 初始化棋盘
    // 初始化
    const initBoard = () => {
      const newBoard = []
      for (let i = 0; i < boardSize; i++) {
        newBoard[i] = []
        for (let j = 0; j < boardSize; j++) {
          newBoard[i][j] = 0
        }
      }
      board.value = newBoard
    }
    
    // 初始化游戏
    onMounted(() => {
      // 获取路由参数
      roomId.value = route.params.roomId || route.query.roomId || ''
      
      // 初始化棋盘
      initBoard()
      
      // 初始化游戏状态
      gameStatus.value = 'waiting'
      currentPlayer.value = 1
      lastMoveRow.value = -1
      lastMoveCol.value = -1
      
      // 获取用户信息
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      if (user.userId) {
        // 连接WebSocket
        connectWebSocket()
      } else {
        ElMessage.error('用户信息缺失，请重新登录')
        router.push('/login')
      }
    })
    
    // 建立WebSocket连接
    // 连接WebSocket
    const connectWebSocket = () => {
      if (websocket.value) {
        websocket.value.close()
      }
      
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
      const userId = currentUser.userId
      
      if (!userId || !roomId.value) {
        ElMessage.error('用户信息或房间ID缺失')
        return
      }
      
      // 使用修复后的API连接WebSocket
      websocket.value = wsManager.connectGameSocket(userId, roomId.value, {
        onOpen: () => {
          console.log('WebSocket连接已建立')
        },
        onMessage: (data) => {
          handleGameMessage(data)
        },
        onError: (error) => {
          console.error('WebSocket错误:', error)
          ElMessage.error('连接服务器失败')
        },
        onClose: () => {
          console.log('WebSocket连接已关闭')
        }
      })
    }

    // 处理游戏消息
    const handleGameMessage = (data) => {
      console.log('收到游戏消息:', data)
      
      // 根据后端API文档，消息格式为 { userId, row, col } 或 { message }
      if (data.userId !== undefined && data.row !== undefined && data.col !== undefined) {
        // 处理落子消息
        const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
        
        // 更新棋盘
        board.value[data.row][data.col] = currentPlayer.value === 1 ? 1 : 2
        
        // 检查是否是自己的落子
        if (data.userId === currentUser.userId) {
          // 更新最后一步位置
          lastMoveRow.value = data.row
          lastMoveCol.value = data.col
          
          // 切换玩家
          currentPlayer.value = currentPlayer.value === 1 ? 2 : 1
        }
      } else if (data.message) {
        // 处理游戏消息
        ElMessage.info(data.message)
        
        // 检查是否是游戏结束消息
        if (data.message.includes('获胜') || data.message.includes('胜利') || data.message.includes('平局')) {
          gameStatus.value = 'ended'
        }
      }
    }

    // 处理棋盘点击
    const handleBoardClick = (row, col) => {
      if (gameStatus.value !== 'playing') {
        ElMessage.warning('游戏尚未开始')
        return
      }
      
      if (board.value[row][col] !== 0) {
        ElMessage.warning('该位置已有棋子')
        return
      }
      
      // 发送落子请求
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
      
      // 根据后端API文档，发送格式为 { userId, row, col }
      const moveData = {
        userId: currentUser.userId,
        row: row,
        col: col
      }
      
      // 使用WebSocket发送落子请求
      if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
        wsManager.sendMessage(websocket.value, moveData)
      } else {
        ElMessage.error('与服务器的连接已断开')
      }
    }

    // 重新开始游戏
    const restartGame = () => {
      gameStatus.value = 'playing'
      currentPlayer.value = 1
      lastMoveRow.value = -1
      lastMoveCol.value = -1
      
      // 重置棋盘
      for (let i = 0; i < boardSize; i++) {
        for (let j = 0; j < boardSize; j++) {
          board.value[i][j] = 0
        }
      }
      
      ElMessage.success('游戏已重新开始')
    }
    
    // 退出游戏
    const exitGame = () => {
      if (websocket.value) {
        websocket.value.close()
      }
      router.push('/')
    }

    // 组件卸载时关闭WebSocket连接
    onUnmounted(() => {
      if (websocket.value) {
        websocket.value.close()
      }
      // 关闭所有WebSocket连接
      wsManager.closeAll()
    })

    return {
      // 游戏状态
      boardSize,
      cellSize,
      board,
      currentPlayer,
      gameStatus,
      lastMoveRow,
      lastMoveCol,
      
      // 玩家信息
      user1,
      user2,
      
      // 方法
      handleBoardClick,
      restartGame,
      exitGame
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