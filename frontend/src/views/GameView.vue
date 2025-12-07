<template>
  <div class="game-container">
    <header class="game-header">
      <h2>五子棋游戏</h2>
      <div class="game-info">
        <span>{{ currentPlayer === 1 ? '当前玩家：黑方' : '当前玩家：白方' }}</span>
        <span>{{ gameStatus }}</span>
      </div>
    </header>
    <main class="game-content">
      <div class="board-container">
        <div class="board" @click="handleBoardClick">
          <div 
            v-for="(row, rowIndex) in boardSize" 
            :key="'row-' + rowIndex" 
            class="board-row"
          >
            <div 
              v-for="(col, colIndex) in boardSize" 
              :key="'col-' + colIndex"
              class="board-cell"
              :class="{
                'black': board[rowIndex][colIndex] === 1,
                'white': board[rowIndex][colIndex] === 2
              }"
            ></div>
          </div>
        </div>
      </div>
    </main>
    <footer class="game-footer">
      <el-button @click="exitGame">退出游戏</el-button>
      <el-button @click="restartGame">重新开始</el-button>
    </footer>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'GameView',
  setup() {
    const router = useRouter()
    const boardSize = 15
    const board = ref([])
    const currentPlayer = ref(1) // 1: 黑方, 2: 白方
    const gameStatus = ref('游戏进行中')
    const websocket = ref(null)
    const roomId = ref(1) // 默认房间ID，实际应该从路由参数或匹配结果获取

    // 初始化棋盘
    const initBoard = () => {
      board.value = Array(boardSize).fill(0).map(() => Array(boardSize).fill(0))
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
          break
        case 'MOVE':
          board.value[data.row][data.col] = data.player
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
      
      const cellSize = rect.width / boardSize
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
      board,
      currentPlayer,
      gameStatus,
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
}

.game-header {
  background-color: #409eff;
  color: white;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.game-info {
  display: flex;
  gap: 20px;
}

.game-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.board-container {
  padding: 20px;
}

.board {
  display: flex;
  flex-direction: column;
  border: 2px solid #333;
  background-color: #f0d9b5;
}

.board-row {
  display: flex;
}

.board-cell {
  width: 30px;
  height: 30px;
  border: 1px solid #8b4513;
  box-sizing: border-box;
  cursor: pointer;
  position: relative;
}

.board-cell:hover {
  background-color: rgba(139, 69, 19, 0.2);
}

.board-cell.black::after {
  content: '';
  position: absolute;
  width: 22px;
  height: 22px;
  background-color: #000;
  border-radius: 50%;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.board-cell.white::after {
  content: '';
  position: absolute;
  width: 22px;
  height: 22px;
  background-color: #fff;
  border-radius: 50%;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border: 1px solid #333;
}

.game-footer {
  padding: 10px 20px;
  background-color: #fff;
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>
