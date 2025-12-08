<template>
  <div class="home-container">
    <!-- 顶部用户信息栏 -->
    <div class="user-bar" v-if="userInfo">
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

    <!-- 未登录状态 -->
    <div class="user-bar guest" v-else>
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
          class="mode-card" 
          :class="{ active: selectedGameMode === 'gobang' }"
          @click="selectGameMode('gobang')"
        >
          <div class="mode-icon">⚫</div>
          <h3>五子棋</h3>
          <p class="mode-description">经典五子棋对战</p>
          <div class="mode-stats" v-if="gameStats.gobang">
            <span class="online-count">在线: {{ gameStats.gobang.online || 0 }}</span>
            <span class="game-count">对局: {{ gameStats.gobang.activeGames || 0 }}</span>
          </div>
        </div>
        
        <div 
          class="mode-card" 
          :class="{ active: selectedGameMode === 'military' }"
          @click="selectGameMode('military')"
        >
          <div class="mode-icon">⚔️</div>
          <h3>军棋</h3>
          <p class="mode-description">策略军旗对战</p>
          <div class="mode-stats" v-if="gameStats.military">
            <span class="online-count">在线: {{ gameStats.military.online || 0 }}</span>
            <span class="game-count">对局: {{ gameStats.military.activeGames || 0 }}</span>
          </div>
        </div>
        
        <div 
          class="mode-card" 
          :class="{ active: selectedGameMode === 'chinese-chess' }"
          @click="selectGameMode('chinese-chess')"
        >
          <div class="mode-icon">♟️</div>
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
        >
          快速匹配
        </el-button>
        <el-button 
          type="success" 
          size="large" 
          @click="createRoom"
          :loading="creatingRoom"
          :disabled="!userInfo"
        >
          创建房间
        </el-button>
        <el-button 
          type="warning" 
          size="large" 
          @click="joinRoom"
          :disabled="!userInfo"
        >
          加入房间
        </el-button>
      </div>

      <!-- 房间列表 -->
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

  <!-- 加入房间对话框 -->
  <el-dialog
    v-model="joinRoomDialog.visible"
    title="加入房间"
    width="400px"
    :close-on-click-modal="false"
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { gameAPI, userAPI } from '../api/index.js'

export default {
  name: 'HomeView',
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

    // 获取用户信息
    const loadUserInfo = async () => {
      try {
        const token = localStorage.getItem('token')
        if (token) {
          const savedUserInfo = localStorage.getItem('userInfo')
          if (savedUserInfo) {
            userInfo.value = JSON.parse(savedUserInfo)
          }
          
          // 尝试从服务器获取最新用户信息
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

    // 获取游戏统计
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

    // 获取房间列表
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

    // 选择游戏模式
    const selectGameMode = (mode) => {
      selectedGameMode.value = mode
      loadRooms()
    }

    // 获取游戏模式标题
    const getGameModeTitle = (mode) => {
      const titles = {
        'gobang': '五子棋',
        'military': '军棋',
        'chinese-chess': '中国象棋'
      }
      return titles[mode] || '未知游戏'
    }

    // 获取游戏模式描述
    const getGameModeDescription = (mode) => {
      const descriptions = {
        'gobang': '经典的五子棋游戏，黑白双方轮流下棋，先连成五子者获胜。考验您的策略和布局能力。',
        'military': '军旗暗棋，通过策略和推理来找出对方的军旗并保护自己的军旗。',
        'chinese-chess': '中国传统象棋，楚河汉界，车马炮象士将，展现您的棋艺。'
      }
      return descriptions[mode] || '精彩的对弈游戏'
    }

    // 获取游戏模式提示
    const getGameModeTips = (mode) => {
      const tips = {
        'gobang': '💡 提示：注意防守和进攻的平衡，既要阻止对手连成四子，也要为自己创造机会。',
        'military': '💡 提示：合理利用炸弹和工兵，保护好您的军旗。',
        'chinese-chess': '💡 提示：马走日，象走田，车行直路炮翻山，士走斜线护将边。'
      }
      return tips[mode] || '祝您游戏愉快！'
    }

    // 获取段位名称
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

    // 获取胜率
    const getWinRate = (user) => {
      if (!user || !user.totalGames) return '0%'
      return Math.round((user.winGames || 0) / user.totalGames * 100) + '%'
    }

    // 获取房间状态
    const getRoomStatus = (status) => {
      const statuses = {
        'waiting': '等待中',
        'playing': '游戏中',
        'full': '已满'
      }
      return statuses[status] || '未知'
    }

    // 快速匹配
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

    // 创建房间
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

    // 加入房间
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

    // 确认加入房间
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

    // 加入特定房间
    const joinSpecificRoom = (roomId) => {
      joinRoomDialog.form.roomId = roomId
      joinRoomDialog.visible = true
    }

    // 查看个人资料
    const viewProfile = () => {
      router.push('/profile')
    }

    // 查看排行榜
    const viewRankings = () => {
      router.push('/rankings')
    }

    // 退出登录
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

    // 定时刷新数据
    let statsTimer = null
    let roomsTimer = null

    const startDataRefresh = () => {
      // 每30秒刷新游戏统计
      statsTimer = setInterval(() => {
        loadGameStats()
      }, 30000)
      
      // 每5秒刷新房间列表（当选择了游戏模式时）
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  position: relative;
  overflow-x: hidden;
}

.home-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  animation: float 6s ease-in-out infinite;
  pointer-events: none;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* 顶部用户信息栏 */
.user-bar {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 20px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
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
  border: 3px solid #667eea;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  color: #333;
}

.username {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.user-stats {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.user-stats span {
  padding: 2px 8px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.user-actions {
  display: flex;
  gap: 10px;
}

.guest-info {
  font-size: 18px;
  color: #333;
  margin-right: 20px;
}

.guest-actions {
  display: flex;
  gap: 10px;
}

/* 游戏模式选择 */
.game-modes {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 3rem;
  color: white;
  margin-bottom: 3rem;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
  animation: fadeInUp 1s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.mode-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.mode-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2.5rem 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.mode-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.mode-card:hover::before {
  left: 100%;
}

.mode-card:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.mode-card.active {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.95), rgba(255, 237, 78, 0.95));
  border-color: #ffd700;
  box-shadow: 0 0 30px rgba(255, 215, 0, 0.3);
}

.mode-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  display: block;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

.mode-card h3 {
  font-size: 1.8rem;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 600;
}

.mode-description {
  color: #666;
  font-size: 1rem;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.mode-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.mode-stats span {
  font-size: 0.9rem;
  color: #888;
  font-weight: 500;
}

/* 游戏操作区域 */
.game-actions {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2.5rem;
  max-width: 800px;
  margin: 0 auto;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.game-actions h2 {
  font-size: 2.2rem;
  color: #333;
  margin-bottom: 1rem;
  font-weight: 600;
}

.game-description {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.game-tips {
  color: #888;
  font-size: 0.95rem;
  margin-bottom: 2rem;
  padding: 10px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 2rem;
}

.action-buttons .el-button {
  font-size: 1.1rem;
  padding: 15px 30px;
  border-radius: 25px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 140px;
}

.action-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 房间列表 */
.rooms-section {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.rooms-section h3 {
  color: #333;
  font-size: 1.5rem;
  margin-bottom: 1rem;
  font-weight: 600;
}

.rooms-list {
  max-height: 300px;
  overflow-y: auto;
}

.room-item {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.room-item:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateX(5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.room-id {
  font-weight: 600;
  color: #333;
  font-size: 1.1rem;
}

.room-players {
  color: #666;
  font-size: 0.9rem;
}

.room-status {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.room-status.waiting {
  background: #67c23a;
  color: white;
}

.room-status.playing {
  background: #e6a23c;
  color: white;
}

.room-status.full {
  background: #f56c6c;
  color: white;
}

.room-details {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: #888;
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .home-container {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  }
  
  .user-bar {
    background: rgba(30, 30, 50, 0.95);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .user-details, .username {
    color: white;
  }
  
  .user-stats span {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
  }
  
  .guest-info {
    color: white;
  }
  
  .title {
    color: white;
  }
  
  .mode-card {
    background: rgba(30, 30, 50, 0.95);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .mode-card h3 {
    color: white;
  }
  
  .mode-description {
    color: #ccc;
  }
  
  .mode-stats span {
    color: #aaa;
  }
  
  .game-actions {
    background: rgba(30, 30, 50, 0.95);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .game-actions h2 {
    color: white;
  }
  
  .game-description {
    color: #ccc;
  }
  
  .game-tips {
    color: #aaa;
    background: rgba(255, 255, 255, 0.05);
    border-left-color: rgba(102, 126, 234, 0.5);
  }
  
  .rooms-section h3 {
    color: white;
  }
  
  .room-item {
    background: rgba(30, 30, 50, 0.8);
    color: white;
  }
  
  .room-id, .room-players, .room-details {
    color: white;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-container {
    padding: 15px;
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
    padding: 1px 6px;
  }
}
</style>