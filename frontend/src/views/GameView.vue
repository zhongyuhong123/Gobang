<template>
  <div class="game-container">
    <!-- 通用导航栏 -->
    <GameNavbar 
      title="五子棋对战"
      :showBackButton="true"
    />
    
    <!-- 游戏信息栏 -->
    <div class="game-info-bar glass-effect">
      <div class="game-info">
        <div class="info-item player-info">
          <span class="label">当前玩家:</span>
          <span class="value" :class="currentPlayer === 1 ? 'black-player' : 'white-player'">
            {{ currentPlayer === 1 ? '黑方' : '白方' }}
          </span>
        </div>
        <div class="info-item status">
          <span class="label">状态:</span>
          <span class="value">
            <span v-if="gameStatus === 'waiting'">等待游戏开始</span>
            <span v-else-if="gameStatus === 'playing'">游戏进行中</span>
            <span v-else-if="gameStatus === 'ended'">游戏已结束</span>
          </span>
        </div>
      </div>
    </div>

    <div class="game-content">
      <div class="game-board-wrapper">
        <div class="board-container">
          <div class="board" @click="handleBoardClick">
            <!-- 棋盘线 -->
            <div class="board-lines">
              <div v-for="i in 14" :key="'h-' + i" class="horizontal-line" :style="{ top: `${i * (100 / 15)}%` }"></div>
              <div v-for="i in 14" :key="'v-' + i" class="vertical-line" :style="{ left: `${i * (100 / 15)}%` }"></div>
              
              <!-- 星位 -->
              <div class="star-point" style="top: 3*(100/15)%; left: 3*(100/15)%;"></div>
              <div class="star-point" style="top: 3*(100/15)%; left: 7*(100/15)%;"></div>
              <div class="star-point" style="top: 3*(100/15)%; left: 11*(100/15)%;"></div>
              <div class="star-point" style="top: 7*(100/15)%; left: 3*(100/15)%;"></div>
              <div class="star-point" style="top: 7*(100/15)%; left: 7*(100/15)%;"></div>
              <div class="star-point" style="top: 7*(100/15)%; left: 11*(100/15)%;"></div>
              <div class="star-point" style="top: 11*(100/15)%; left: 3*(100/15)%;"></div>
              <div class="star-point" style="top: 11*(100/15)%; left: 7*(100/15)%;"></div>
              <div class="star-point" style="top: 11*(100/15)%; left: 11*(100/15)%;"></div>
            </div>
            
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
          <button class="btn primary control-button" @click="restartGame">重新开始</button>
          <button class="btn secondary control-button" @click="exitGame">退出游戏</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { wsManager } from '../api/index'
import GameNavbar from '@/components/GameNavbar.vue'

export default {
  name: 'GameView',
  components: {
    GameNavbar
  },
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

.white-player {
  /* 使用稍暗的白色以提高对比度 */
  color: rgba(255, 255, 255, 0.95);
  font-weight: 700;
  /* 增强文字阴影以提高可读性 */
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

/* 游戏信息栏样式 */
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
  /* 使用稍暗的白色以提高对比度 */
  color: rgba(255, 255, 255, 0.95);
  /* 增强文字阴影以提高可读性 */
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.5), 0 0 10px rgba(0, 0, 0, 0.2);
}

/* 游戏棋盘样式 */
.board-container {
  padding: 20px;
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
  box-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
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
  transition: background-color 0.2s ease;
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

/* 最后一步高亮 */
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

/* 响应式设计 */
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