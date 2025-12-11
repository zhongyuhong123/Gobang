<template>
  <div class="home-container">
    <GameNavbar />
    
    <!-- 顶部用户信息栏 -->
    <div class="user-bar glass-user-bar" v-if="userInfo">
      <div class="user-info">
        <div class="user-avatar">
          <img :src="userInfo.avatar || '/default-avatar.png'" alt="头像" />
        </div>
        <div class="user-details">
          <div class="username">{{ userInfo.username }}</div>
          <div class="user-stats">
            <span class="points">积分: {{ userInfo.points || 0 }}</span>
            <span class="rank">段位: {{ getRankName(userInfo.rank || 0) }}</span>
            <span class="win-rate">胜率: {{ getWinRate(userInfo) }}</span>
          </div>
        </div>
      </div>
      <div class="user-actions">
        <el-button type="primary" size="small" @click="viewProfile">个人资料</el-button>
        <el-button type="warning" size="small" @click="viewRankings">排行榜</el-button>
        <el-button type="danger" size="small" @click="logout">退出登录</el-button>
      </div>
    </div>

    <div class="user-bar guest glass-user-bar" v-else>
      <div class="guest-info">
        <span>欢迎来到五子棋游戏大厅！</span>
      </div>
      <div class="guest-actions">
        <el-button type="primary" size="small" @click="$router.push('/login')">登录</el-button>
        <el-button type="success" size="small" @click="$router.push('/register')">注册</el-button>
      </div>
    </div>

    <div class="game-modes">
      <h1 class="title">选择游戏模式</h1>
      <div class="mode-cards">
        <div 
          class="mode-card glass-game-card" 
          :class="{ active: selectedGameMode === 'gobang' }"
          @click="selectGameMode('gobang')"
        >
          <h3>五子棋</h3>
          <p class="mode-description">经典五子棋对战</p>
          <div class="mode-stats" v-if="gameStats.gobang">
            <span class="online-count">在线: {{ gameStats.gobang.online || 0 }}</span>
            <span class="game-count">对局: {{ gameStats.gobang.activeGames || 0 }}</span>
          </div>
        </div>
        
        <div 
          class="mode-card glass-game-card" 
          :class="{ active: selectedGameMode === 'military' }"
          @click="selectGameMode('military')"
        >
          <h3>军棋</h3>
          <p class="mode-description">策略军旗对战</p>
          <div class="mode-stats" v-if="gameStats.military">
            <span class="online-count">在线: {{ gameStats.military.online || 0 }}</span>
            <span class="game-count">对局: {{ gameStats.military.activeGames || 0 }}</span>
          </div>
        </div>
        
        <div 
          class="mode-card glass-game-card" 
          :class="{ active: selectedGameMode === 'chinese-chess' }"
          @click="selectGameMode('chinese-chess')"
        >
          <h3>中国象棋</h3>
          <p class="mode-description">传统象棋对弈</p>
          <div class="mode-stats" v-if="gameStats.chineseChess">
            <span class="online-count">在线: {{ gameStats.chineseChess.online || 0 }}</span>
            <span class="game-count">对局: {{ gameStats.chineseChess.activeGames || 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="game-actions" v-if="selectedGameMode">
      <h2>{{ getGameModeTitle(selectedGameMode) }}</h2>
      <p class="game-description">{{ getGameModeDescription(selectedGameMode) }}</p>
      <p class="game-tips">{{ getGameModeTips(selectedGameMode) }}</p>
      
      <div class="action-buttons">
        <el-button 
          type="primary" 
          size="large" 
          @click="startQuickMatch"
          :loading="matchingLoading"
          :disabled="!userInfo"
          class="glass-button"
        >
          快速匹配
        </el-button>
        <el-button 
          type="success" 
          size="large" 
          @click="createRoom"
          :loading="creatingRoom"
          :disabled="!userInfo"
          class="glass-button"
        >
          创建房间
        </el-button>
        <el-button 
          type="warning" 
          size="large" 
          @click="joinRoom"
          :disabled="!userInfo"
          class="glass-button"
        >
          加入房间
        </el-button>
      </div>

        <div class="rooms-section" v-if="rooms.length > 0">
        <h3>活跃房间</h3>
        <div class="rooms-list">
          <div 
            class="room-item" 
            v-for="room in rooms" 
            :key="room.id"
            @click="joinSpecificRoom(room.id)"
          >
            <div class="room-info">
              <span class="room-id">房间 {{ room.id }}</span>
              <span class="room-players">{{ room.currentPlayers }}/{{ room.maxPlayers }}</span>
              <span class="room-status" :class="room.status">{{ getRoomStatus(room.status) }}</span>
            </div>
            <div class="room-details">
              <span class="room-host">房主: {{ room.hostName }}</span>
              <span class="room-created">{{ room.createdAt }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-dialog
    v-model="joinRoomDialog.visible"
    title="加入房间"
    width="400px"
    :close-on-click-modal="false"
    custom-class="glass-modal"
  >
    <el-form :model="joinRoomDialog.form" label-width="80px">
      <el-form-item label="房间号">
        <el-input 
          v-model="joinRoomDialog.form.roomId" 
          placeholder="请输入房间号"
          maxlength="10"
        />
      </el-form-item>
      <el-form-item label="房间密码" v-if="joinRoomDialog.form.requirePassword">
        <el-input 
          v-model="joinRoomDialog.form.password" 
          placeholder="请输入房间密码（可选）"
          type="password"
          show-password
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="joinRoomDialog.visible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmJoinRoom"
          :loading="joinRoomDialog.loading"
        >
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { gameAPI, userAPI } from '../api/index.js'
import GameNavbar from '../components/GameNavbar.vue'

export default {
  name: 'HomeView',
  components: {
    GameNavbar
  },
  setup() {
    const router = useRouter()
    const selectedGameMode = ref(null)
    const matchingLoading = ref(false)
    const creatingRoom = ref(false)
    
    // 用户信息
    const userInfo = ref(null)
    
    // 游戏统计
    const gameStats = reactive({
      gobang: null,
      military: null,
      chineseChess: null
    })

    onUnmounted(() => {
      stopDataRefresh()
    })
    
    // 房间列表
    const rooms = ref([])
    
    // 加入房间对话框
    const joinRoomDialog = reactive({
      visible: false,
      loading: false,
      form: {
        roomId: '',
        password: '',
        requirePassword: false
      }
    })

    const loadUserInfo = async () => {
      try {
        const token = localStorage.getItem('token')
        if (token) {
          const savedUserInfo = localStorage.getItem('userInfo')
          if (savedUserInfo) {
            userInfo.value = JSON.parse(savedUserInfo)
          }
          
          const response = await userAPI.getUserInfo()
          if (response.success && response.data) {
            userInfo.value = response.data
            localStorage.setItem('userInfo', JSON.stringify(response.data))
          }
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }

    const loadGameStats = async () => {
      try {
        const response = await gameAPI.getGameStats()
        if (response.success && response.data) {
          Object.assign(gameStats, response.data)
        }
      } catch (error) {
        console.error('获取游戏统计失败:', error)
      }
    }

    const loadRooms = async () => {
      if (!selectedGameMode.value) return
      
      try {
        const response = await gameAPI.getRooms(selectedGameMode.value)
        if (response.success && response.data) {
          rooms.value = response.data
        }
      } catch (error) {
        console.error('获取房间列表失败:', error)
      }
    }

    const selectGameMode = (mode) => {
      selectedGameMode.value = mode
      loadRooms()
    }

    const getGameModeTitle = (mode) => {
      const titles = {
        'gobang': '五子棋',
        'military': '军棋',
        'chinese-chess': '中国象棋'
      }
      return titles[mode] || '未知游戏'
    }

    const getGameModeDescription = (mode) => {
      const descriptions = {
        'gobang': '经典的五子棋游戏，黑白双方轮流下棋，先连成五子者获胜。考验您的策略和布局能力。',
        'military': '军旗暗棋，通过策略和推理来找出对方的军旗并保护自己的军旗。',
        'chinese-chess': '中国传统象棋，楚河汉界，车马炮象士将，展现您的棋艺。'
      }
      return descriptions[mode] || '精彩的对弈游戏'
    }

    const getGameModeTips = (mode) => {
      const tips = {
        'gobang': '提示：注意防守和进攻的平衡，既要阻止对手连成四子，也要为自己创造机会。',
        'military': '提示：合理利用炸弹和工兵，保护好您的军旗。',
        'chinese-chess': '提示：马走日，象走田，车行直路炮翻山，士走斜线护将边。'
      }
      return tips[mode] || '祝您游戏愉快！'
    }

    const getRankName = (rank) => {
      const ranks = {
        0: '新手',
        1: '初级',
        2: '中级',
        3: '高级',
        4: '专家',
        5: '大师',
        6: '宗师'
      }
      return ranks[rank] || '新手'
    }

    const getWinRate = (user) => {
      if (!user || !user.totalGames) return '0%'
      return Math.round((user.winGames || 0) / user.totalGames * 100) + '%'
    }

    const getRoomStatus = (status) => {
      const statuses = {
        'waiting': '等待中',
        'playing': '游戏中',
        'full': '已满'
      }
      return statuses[status] || '未知'
    }

    const startQuickMatch = async () => {
      if (!selectedGameMode.value) return
      if (!userInfo.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }

      matchingLoading.value = true
      try {
        const response = await gameAPI.quickMatch(selectedGameMode.value)
        if (response.success) {
          ElMessage.success('匹配成功，正在进入游戏...')
          // 跳转到游戏页面
          router.push(`/game/${response.data.gameId}`)
        } else {
          ElMessage.error(response.message || '匹配失败')
        }
      } catch (error) {
        console.error('快速匹配失败:', error)
        ElMessage.error('匹配失败，请重试')
      } finally {
        matchingLoading.value = false
      }
    }

    const createRoom = async () => {
      if (!selectedGameMode.value) return
      if (!userInfo.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }

      creatingRoom.value = true
      try {
        const response = await gameAPI.createRoom(selectedGameMode.value)
        if (response.success) {
          ElMessage.success('房间创建成功')
          // 跳转到房间页面
          router.push(`/room/${response.data.roomId}`)
        } else {
          ElMessage.error(response.message || '创建房间失败')
        }
      } catch (error) {
        console.error('创建房间失败:', error)
        ElMessage.error('创建房间失败，请重试')
      } finally {
        creatingRoom.value = false
      }
    }

    const joinRoom = () => {
      if (!selectedGameMode.value) return
      if (!userInfo.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }

      joinRoomDialog.visible = true
      joinRoomDialog.form.roomId = ''
      joinRoomDialog.form.password = ''
    }

    const confirmJoinRoom = async () => {
      if (!joinRoomDialog.form.roomId.trim()) {
        ElMessage.warning('请输入房间号')
        return
      }

      joinRoomDialog.loading = true
      try {
        const response = await gameAPI.joinRoom({
          roomId: joinRoomDialog.form.roomId.trim(),
          password: joinRoomDialog.form.password
        })
        
        if (response.success) {
          ElMessage.success('加入房间成功')
          joinRoomDialog.visible = false
          // 跳转到房间页面
          router.push(`/room/${joinRoomDialog.form.roomId}`)
        } else {
          ElMessage.error(response.message || '加入房间失败')
        }
      } catch (error) {
        console.error('加入房间失败:', error)
        ElMessage.error('加入房间失败，请重试')
      } finally {
        joinRoomDialog.loading = false
      }
    }

    const joinSpecificRoom = (roomId) => {
      joinRoomDialog.form.roomId = roomId
      joinRoomDialog.visible = true
    }

    const viewProfile = () => {
      router.push('/profile')
    }

    const viewRankings = () => {
      router.push('/rankings')
    }

    const logout = () => {
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('rememberedUsername')
        userInfo.value = null
        ElMessage.success('退出登录成功')
        router.push('/login')
      }).catch(() => {
        // 取消退出
      })
    }

    let statsTimer = null
    let roomsTimer = null

    const startDataRefresh = () => {
      statsTimer = setInterval(() => {
        loadGameStats()
      }, 30000)
      
      roomsTimer = setInterval(() => {
        if (selectedGameMode.value) {
          loadRooms()
        }
      }, 5000)
    }

    const stopDataRefresh = () => {
      if (statsTimer) {
        clearInterval(statsTimer)
        statsTimer = null
      }
      if (roomsTimer) {
        clearInterval(roomsTimer)
        roomsTimer = null
      }
    }

    onMounted(() => {
      loadUserInfo()
      loadGameStats()
      startDataRefresh()
    })

    // 组件卸载时停止定时器
    onUnmounted(() => {
      stopDataRefresh()
    })

    // 组件卸载时停止定时器
    onUnmounted(() => {
      stopDataRefresh()
    })

    return {
      selectedGameMode,
      matchingLoading,
      creatingRoom,
      userInfo,
      gameStats,
      rooms,
      joinRoomDialog,
      selectGameMode,
      getGameModeTitle,
      getGameModeDescription,
      getGameModeTips,
      getRankName,
      getWinRate,
      getRoomStatus,
      startQuickMatch,
      createRoom,
      joinRoom,
      confirmJoinRoom,
      joinSpecificRoom,
      viewProfile,
      viewRankings,
      logout
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: var(--color-background-primary);
  padding: 0;
  padding-top: 80px;
}

.user-bar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  padding: 24px 30px;
  margin: 20px;
  margin-bottom: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-xl);
  border: 1px solid rgba(255, 255, 255, 0.25);
}

.user-bar.guest {
  justify-content: center;
  text-align: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  color: var(--color-text-primary);
}

.username {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
  color: var(--color-text-primary);
}

.user-stats {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.user-stats span {
  padding: 4px 10px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
}

.user-actions {
  display: flex;
  gap: 10px;
}

.guest-info {
  font-size: 18px;
  color: var(--color-text-primary);
  margin-right: 20px;
}

.guest-actions {
  display: flex;
  gap: 10px;
}

.game-modes {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 3.5rem;
  color: var(--color-text-primary);
  margin-bottom: 3rem;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-text-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.mode-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 2rem;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px;
}

.mode-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  padding: 2.5rem 2rem;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(255, 255, 255, 0.25);
  position: relative;
  overflow: hidden;
}

.mode-card.active {
  background: rgba(64, 158, 255, 0.1);
  border-color: var(--color-primary);
  box-shadow: 0 0 30px rgba(64, 158, 255, 0.2), 0 0 0 2px rgba(64, 158, 255, 0.3);
}

.mode-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  display: block;
}

.mode-card h3 {
  font-size: 1.8rem;
  margin-bottom: 0.5rem;
  color: var(--color-text-primary);
  font-weight: 800;
  letter-spacing: -0.5px;
}

.mode-description {
  color: var(--color-text-secondary);
  font-size: 0.95rem;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.mode-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border);
}

.mode-stats span {
  font-size: 0.85rem;
  color: var(--color-text-tertiary);
  font-weight: 500;
}

/* 游戏操作区域 */
.game-actions {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: var(--radius-xl);
  padding: 3rem 2.5rem;
  max-width: 900px;
  margin: 40px auto;
  box-shadow: var(--shadow-xl);
  text-align: center;
}

.game-actions h2 {
  font-size: 1.8rem;
  color: var(--color-text-primary);
  margin-bottom: 1rem;
  font-weight: 600;
}

.game-description {
  color: var(--color-text-secondary);
  font-size: 1rem;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.game-tips {
  color: var(--color-text-tertiary);
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  padding: 12px 16px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-small);
  border-left: 4px solid var(--color-primary);
  text-align: left;
}

.action-buttons {
  display: flex;
  gap: 1.5rem;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 2rem;
}

.action-buttons .el-button {
  font-size: 1.1rem;
  padding: 15px 30px;
  border-radius: var(--radius-lg);
  font-weight: 700;
  min-width: 140px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.1);
  position: relative;
  overflow: hidden;
}

.action-buttons .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, transparent 50%);
  pointer-events: none;
}

.action-buttons .el-button--primary {
  background: rgba(64, 158, 255, 0.8);
  color: white;
  border-color: rgba(255, 255, 255, 0.25);
}

.action-buttons .el-button--primary {
  background: rgba(64, 158, 255, 0.8);
  color: white;
  border-color: rgba(255, 255, 255, 0.25);
}

.action-buttons .el-button--success {
  background: rgba(103, 194, 58, 0.8);
  color: white;
  border-color: rgba(255, 255, 255, 0.25);
}

.action-buttons .el-button--warning {
  background: rgba(230, 162, 60, 0.8);
  color: white;
  border-color: rgba(255, 255, 255, 0.25);
}

/* 房间列表 */
.rooms-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--color-border);
}

.rooms-section h3 {
  color: var(--color-text-primary);
  font-size: 1.3rem;
  margin-bottom: 1rem;
  font-weight: 600;
}

.rooms-list {
  max-height: 300px;
  overflow-y: auto;
}

.room-item {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--radius-small);
  padding: 0.75rem;
  margin-bottom: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px 0 rgba(31, 38, 135, 0.2);
}

.room-item:hover {
  background: var(--color-background-hover);
  transform: translateX(4px);
  box-shadow: var(--shadow-sm);
  border-color: var(--color-primary);
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.room-id {
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: 1rem;
}

.room-players {
  color: var(--color-text-secondary);
  font-size: 0.85rem;
}

.room-status {
  padding: 3px 8px;
  border-radius: var(--radius-pill);
  font-size: 0.75rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.room-status.waiting {
  background: rgba(103, 194, 58, 0.9);
  color: white;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.room-status.playing {
  background: rgba(230, 162, 60, 0.9);
  color: white;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.room-status.full {
  background: rgba(245, 108, 108, 0.9);
  color: white;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.room-details {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: var(--color-text-tertiary);
}



/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .home-container {
    background: #1f1f1f;
  }
  
  .user-bar {
    background: #2d2d2d;
    color: #e8e8e8;
    border: 1px solid #444;
  }
  
  .user-details, .username {
    color: #e8e8e8;
  }
  
  .user-stats span {
    background: #3a3a3a;
    border: 1px solid #555;
    color: #ccc;
  }
  
  .guest-info {
    color: #e8e8e8;
  }
  
  .title {
    color: #e8e8e8;
  }
  
  .mode-card {
    background: #2d2d2d;
    color: #e8e8e8;
    border: 1px solid #444;
  }
  
  .mode-card h3 {
    color: #e8e8e8;
  }
  
  .mode-description {
    color: #bbb;
  }
  
  .mode-stats span {
    color: #999;
  }
  
  .mode-card.active {
    background: #1e3a5f;
    border-color: #409eff;
  }
  
  .game-actions {
    background: #2d2d2d;
    color: #e8e8e8;
    border: 1px solid #444;
  }
  
  .game-actions h2 {
    color: #e8e8e8;
  }
  
  .game-description {
    color: #bbb;
  }
  
  .game-tips {
    color: #999;
    background: #3a3a3a;
    border-left-color: #409eff;
  }
  
  .rooms-section h3 {
    color: #e8e8e8;
  }
  
  .room-item {
    background: #3a3a3a;
    color: #e8e8e8;
    border: 1px solid #555;
  }
  
  
  
  .room-id, .room-players, .room-details {
    color: #e8e8e8;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-container {
    padding: 15px;
    padding-top: 80px;
  }
  
  .user-bar {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .user-info {
    justify-content: center;
  }
  
  .user-stats {
    flex-direction: column;
    gap: 5px;
  }
  
  .title {
    font-size: 2.2rem;
    margin-bottom: 2rem;
  }
  
  .mode-cards {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .mode-card {
    padding: 2rem 1.5rem;
  }
  
  .game-actions {
    padding: 2rem 1.5rem;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .action-buttons .el-button {
    width: 100%;
    max-width: 250px;
  }
  
  .room-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .room-details {
    flex-direction: column;
    gap: 5px;
  }
}

@media (max-width: 480px) {
  .title {
    font-size: 1.8rem;
  }
  
  .mode-card {
    padding: 1.5rem 1rem;
  }
  
  .mode-icon {
    font-size: 3rem;
  }
  
  .game-actions {
    padding: 1.5rem;
  }
  
  .game-actions h2 {
    font-size: 1.8rem;
  }
  
  .user-stats {
    font-size: 12px;
  }
  
  .user-stats span {
    padding: 2px 8px;
  }
}
/* 对话框 */
.glass-modal .el-dialog__header {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.18);
}

.glass-modal .el-dialog__body {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.glass-modal .el-dialog__footer {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.18);
}
</style>