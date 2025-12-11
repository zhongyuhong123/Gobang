<template>
  <div class="home-container">
    <div class="left-nav">
      <div class="nav-icon" @click="goToHome" title="首页">
        <House />
      </div>
      <div class="nav-icon" @click="goToFriends" title="好友">
        <User />
      </div>
      <div class="nav-icon" @click="goToRanking" title="排行榜">
        <Trophy />
      </div>
      <div class="nav-icon" @click="showSettings" title="设置">
        <Setting />
      </div>
    </div>

    <div class="main-content">
      <div class="top-bar">
        <div class="user-info" v-if="userInfo">
          <div class="user-avatar">
            <el-avatar 
              :src="userInfo.avatar || '/default-avatar.png'" 
              :size="48"
              fit="cover"
              @error="() => userInfo.avatar = '/default-avatar.png'">
              <User />
            </el-avatar>
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
        <div class="top-actions">
          <div class="top-stats">
            <span class="online-count">在线: {{ totalOnline }}</span>
            <span class="game-count">对局: {{ totalGames }}</span>
          </div>
          <div class="user-actions" v-if="userInfo">
      </div>
          <div class="guest-actions" v-else>
            <el-button type="primary" size="small" @click="$router.push('/login')">登录</el-button>
            <el-button type="success" size="small" @click="$router.push('/register')">注册</el-button>
          </div>
        </div>
      </div>

      <div class="game-modes">
        <div class="game-header">
      </div>
      </div>

      <div class="game-actions">
        <div class="game-tabs">
          <div class="game-tab" 
               :class="{ active: selectedGameMode === 'gobang' }" 
               @click="selectGameMode('gobang')"
               title="五子棋">
            <span class="tab-icon"><Check /></span>
            <span class="tab-name">五子棋</span>
          </div>

          <div class="game-tab" 
               :class="{ active: selectedGameMode === 'military' }"
               @click="selectGameMode('military')"
               title="军棋">
            <span class="tab-icon"><Flag /></span>
            <span class="tab-name">军棋</span>
          </div>

          <div class="game-tab" 
               :class="{ active: selectedGameMode === 'chinese-chess' }"
               @click="selectGameMode('chinese-chess')"
               title="中国象棋">
            <span class="tab-icon"><PieChart /></span>
            <span class="tab-name">中国象棋</span>
          </div>
        </div>
        <div class="game-info-section">
          <div class="game-title-section">
            <h2>{{ getGameModeTitle(selectedGameMode) }}</h2>
            <div class="game-stats" v-if="getGameStats(selectedGameMode)">
              <span class="stat-item">
                <User />
                {{ getGameStats(selectedGameMode).online || 0 }} 在线
              </span>
              <span class="stat-item">
                <Cpu />
                {{ getGameStats(selectedGameMode).activeGames || 0 }} 对局
              </span>
            </div>
          </div>
          <div class="game-description-section">
            <p class="game-description">{{ getGameModeDescription(selectedGameMode) }}</p>
            <p class="game-tips">{{ getGameModeTips(selectedGameMode) }}</p>
          </div>
        </div>

        <div class="action-buttons">
          <el-button type="primary" size="large" @click="startQuickMatch" :loading="matchingLoading"
            :disabled="!userInfo" class="glass-button">
            <Refresh />
            快速匹配
          </el-button>
          <el-button type="success" size="large" @click="createRoom" :loading="creatingRoom" :disabled="!userInfo"
            class="glass-button">
            <Plus />
            创建房间
          </el-button>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="settingsDialog.visible" title="设置" width="400px" custom-class="glass-modal">
    <div class="settings-content">
      <p>设置功能开发中...</p>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="settingsDialog.visible = false">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style>
/* 全局动画和过渡效果 */
.game-tab.active {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(100, 255, 218, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(100, 255, 218, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(100, 255, 218, 0);
  }
}

/* 图标动画 */
.nav-icon svg {
  transition: transform 0.3s ease;
}

.nav-icon:hover svg {
  transform: scale(1.1);
}

/* 头像悬停效果 */
.user-avatar {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 0 25px rgba(100, 255, 218, 0.3);
}

/* 按钮点击效果 */
.action-buttons .el-button:active {
  transform: scale(0.98);
}

/* 游戏标签悬停动画 */
.game-tab::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(100, 255, 218, 0.1), transparent);
  transition: left 0.5s ease;
}

.game-tab:hover::before {
  left: 100%;
}
</style>

<script>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { gameAPI, userAPI } from '../api/index.js'

export default {
  name: 'HomeView',
  setup() {
    const router = useRouter()
    const selectedGameMode = ref('gobang')
    const matchingLoading = ref(false)
    const creatingRoom = ref(false)

    const userInfo = ref(null)

    const gameStats = reactive({
      gobang: null,
      military: null,
      chineseChess: null
    })

    const totalOnline = ref(0)
    const totalGames = ref(0)

    const updateTotalStats = () => {
      let online = 0
      let games = 0

      if (gameStats.gobang) {
        online += gameStats.gobang.online || 0
        games += gameStats.gobang.activeGames || 0
      }
      if (gameStats.military) {
        online += gameStats.military.online || 0
        games += gameStats.military.activeGames || 0
      }
      if (gameStats.chineseChess) {
        online += gameStats.chineseChess.online || 0
        games += gameStats.chineseChess.activeGames || 0
      }

      totalOnline.value = online
      totalGames.value = games
    }

    const settingsDialog = reactive({
      visible: false
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
        if (error.code === 'ECONNREFUSED' || error.message?.includes('ERR_CONNECTION_REFUSED') ||
          error.message?.includes('用户信息缺失')) {
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          userInfo.value = null
          ElMessage.error('连接服务器失败，请重新登录')
          router.push('/login')
        }
      }
    }

    const loadGameStats = async () => {
      try {
        const response = await gameAPI.getGameStats()
        if (response.success && response.data) {
          Object.assign(gameStats, response.data)
          updateTotalStats()
        }
      } catch (error) {
        console.error('获取游戏统计失败:', error)
        if (error.code === 'ECONNREFUSED' || error.message?.includes('ERR_CONNECTION_REFUSED')) {
          console.warn('服务器连接失败，游戏统计数据加载失败')
        }
      }
    }

    const selectGameMode = (mode) => {
      selectedGameMode.value = mode
    }

    const gameModeConfigs = {
      'gobang': {
        title: '五子棋',
        description: '经典的五子棋游戏，黑白双方轮流下棋，先连成五子者获胜。考验您的策略和布局能力。',
        tips: '提示：注意防守和进攻的平衡，既要阻止对手连成四子，也要为自己创造机会。'
      },
      'military': {
        title: '军棋',
        description: '军旗暗棋，通过策略和推理来找出对方的军旗并保护自己的军旗。',
        tips: '提示：合理利用炸弹和工兵，保护好您的军旗。'
      },
      'chinese-chess': {
        title: '中国象棋',
        description: '中国传统象棋，楚河汉界，车马炮象士将，展现您的棋艺。',
        tips: '提示：马走日，象走田，车行直路炮翻山，士走斜线护将边。'
      }
    }

    const getGameModeTitle = (mode) => {
      return gameModeConfigs[mode]?.title || '未知游戏'
    }

    const getGameModeDescription = (mode) => {
      return gameModeConfigs[mode]?.description || '精彩的对弈游戏'
    }

    const getGameModeTips = (mode) => {
      return gameModeConfigs[mode]?.tips || '祝您游戏愉快！'
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

    const getGameStats = (mode) => {
      const statsMap = {
        'gobang': gameStats.gobang,
        'military': gameStats.military,
        'chinese-chess': gameStats.chineseChess
      }
      return statsMap[mode] || null
    }

    const goToHome = () => {
      router.push('/')
    }
    
    const goToFriends = () => {
      router.push('/friends')
    }
    
    const goToRanking = () => {
      router.push('/ranking')
    }
    
    const showSettings = () => {
      settingsDialog.visible = true
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

    let statsTimer = null

    const startDataRefresh = () => {
      statsTimer = setInterval(() => {
        loadGameStats()
      }, 30000)
    }

    const stopDataRefresh = () => {
      if (statsTimer) {
        clearInterval(statsTimer)
        statsTimer = null
      }
    }

    onMounted(async () => {
      await Promise.all([
        loadUserInfo(),
        loadGameStats()
      ])
      startDataRefresh()
    })

    onUnmounted(() => {
      stopDataRefresh()
    })

    return {
      selectedGameMode,
      matchingLoading,
      creatingRoom,
      userInfo,
      gameStats,
      settingsDialog,
      totalOnline,
      totalGames,
      selectGameMode,
      getGameModeTitle,
      getGameModeDescription,
      getGameModeTips,
      getRankName,
      getWinRate,
      getGameStats,
      startQuickMatch,
      createRoom,
      goToHome,
      goToFriends,
      goToRanking,
      showSettings
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a192f 0%, #172a45 100%);
  display: flex;
  flex-direction: row;
  color: #e8e8e8;
}

.left-nav {
  width: 70px;
  background: rgba(17, 34, 64, 0.95);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(100, 255, 218, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 25px;
  gap: 25px;
  box-shadow: 0 0 25px rgba(0, 0, 0, 0.25);
}

.nav-icon {
  width: 42px;
  height: 42px;
  background: rgba(100, 255, 218, 0.08);
  border: 1px solid rgba(100, 255, 218, 0.2);
  border-radius: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: rgba(100, 255, 218, 0.7);
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-icon:hover {
  background: rgba(100, 255, 218, 0.15);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(100, 255, 218, 0.15);
  color: rgba(100, 255, 218, 0.9);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow-y: auto;
}

.top-bar {
  background: rgba(17, 34, 64, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(100, 255, 218, 0.15);
  border-radius: 14px;
  padding: 16px 24px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(100, 255, 218, 0.4);
  box-shadow: 0 0 16px rgba(100, 255, 218, 0.2);
  cursor: default;
  flex-shrink: 0;
}

.user-details {
  color: #e8e8e8;
  text-align: left;
}

.username {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #e8e8e8;
  letter-spacing: 0.5px;
}

.user-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: rgba(232, 232, 232, 0.7);
}

.user-stats span {
  padding: 4px 10px;
  background: rgba(100, 255, 218, 0.08);
  border-radius: 16px;
  border: 1px solid rgba(100, 255, 218, 0.15);
  font-weight: 500;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.top-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: rgba(232, 232, 232, 0.7);
}

.top-stats span {
  padding: 6px 12px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 20px;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.user-actions,
.guest-actions {
  display: flex;
  gap: 12px;
}

.user-actions .el-button,
.guest-actions .el-button {
  padding: 6px 12px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 20px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  font-size: 14px;
  color: rgba(232, 232, 232, 0.7);
  box-shadow: none;
  transition: all 0.3s ease;
}

.user-actions .el-button:hover,
.guest-actions .el-button:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.3);
  color: rgba(232, 232, 232, 0.9);
}

.user-actions .el-button:active,
.guest-actions .el-button:active {
  background: rgba(64, 158, 255, 0.25);
  border-color: rgba(64, 158, 255, 0.4);
  transform: translateY(1px);
}

.user-actions .el-button:disabled,
.guest-actions .el-button:disabled {
  background: rgba(64, 158, 255, 0.05);
  border-color: rgba(64, 158, 255, 0.1);
  color: rgba(232, 232, 232, 0.4);
  cursor: not-allowed;
}

.game-modes {
  margin-bottom: 30px;
}

.game-header {
  margin-bottom: 0;
}

.game-tabs {
  display: flex;
  gap: 12px;
  background: rgba(17, 34, 64, 0.7);
  backdrop-filter: blur(12px);
  padding: 8px;
  border-radius: 10px;
  border: 1px solid rgba(100, 255, 218, 0.1);
  margin-bottom: 16px;
}

.game-tab {
  padding: 10px 18px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: transparent;
  border: 1px solid transparent;
  color: rgba(232, 232, 232, 0.7);
  position: relative;
  overflow: hidden;
}

.game-tab:hover {
  background: rgba(100, 255, 218, 0.08);
  color: rgba(100, 255, 218, 0.9);
  border-color: rgba(100, 255, 218, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(100, 255, 218, 0.15);
}

.game-tab.active {
  background: rgba(100, 255, 218, 0.15);
  border: 1px solid rgba(100, 255, 218, 0.4);
  color: rgba(100, 255, 218, 1);
  box-shadow: 0 5px 15px rgba(100, 255, 218, 0.2);
  transform: scale(1.05);
  box-shadow: 0 0 20px rgba(100, 255, 218, 0.25);
}

.game-tab.active::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(100, 255, 218, 0.2), transparent);
  animation: shine 1.5s infinite;
}

@keyframes shine {
  0% { left: -100%; }
  100% { left: 100%; }
}

.tab-icon {
  font-size: 16px;
  display: flex;
  align-items: center;
}

.tab-name {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.game-actions {
  background: rgba(17, 34, 64, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(100, 255, 218, 0.15);
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
}

.game-info-section {
  margin-bottom: 30px;
}

.game-title-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.game-title-section h2 {
  font-size: 2rem;
  color: #e8e8e8;
  font-weight: 700;
  margin: 0;
}

.game-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(64, 158, 255, 0.1);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 20px;
  color: rgba(64, 158, 255, 0.9);
  font-size: 14px;
  font-weight: 500;
}

.game-description-section {
  background: rgba(100, 255, 218, 0.05);
  border: 1px solid rgba(100, 255, 218, 0.1);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.game-description {
  color: rgba(232, 232, 232, 0.8);
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 15px;
}

.game-tips {
  color: rgba(100, 255, 218, 0.8);
  font-size: 14px;
  padding-left: 20px;
  border-left: 3px solid rgba(100, 255, 218, 0.5);
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  font-size: 1.1rem;
  padding: 18px 32px;
  border-radius: 12px;
  font-weight: 700;
  min-width: 160px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background: rgba(64, 158, 255, 0.15);
  border: 1px solid rgba(64, 158, 255, 0.4);
  color: rgba(64, 158, 255, 0.9);
  transition: all 0.3s ease;
  box-shadow: 0 5px 20px rgba(64, 158, 255, 0.1);
}

.action-buttons .el-button:hover {
  background: rgba(64, 158, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(64, 158, 255, 0.2);
}

.action-buttons .el-button--primary {
  background: rgba(64, 158, 255, 0.8);
  color: white;
  border-color: rgba(64, 158, 255, 0.5);
}

.action-buttons .el-button--primary:hover {
  background: rgba(64, 158, 255, 0.9);
  box-shadow: 0 8px 30px rgba(64, 158, 255, 0.4);
}

.action-buttons .el-button--success {
  background: rgba(103, 194, 58, 0.8);
  color: white;
  border-color: rgba(103, 194, 58, 0.5);
}

.action-buttons .el-button--success:hover {
  background: rgba(103, 194, 58, 0.9);
  box-shadow: 0 8px 30px rgba(103, 194, 58, 0.4);
}

.action-buttons .el-button i {
  margin-right: 8px;
  font-size: 18px;
}

.settings-content {
  padding: 20px 0;
  text-align: center;
  color: rgba(232, 232, 232, 0.8);
}

.glass-modal .el-dialog__header {
  background: rgba(17, 34, 64, 0.9);
  backdrop-filter: blur(15px);
  border-bottom: 1px solid rgba(100, 255, 218, 0.2);
}

.glass-modal .el-dialog__body {
  background: rgba(17, 34, 64, 0.9);
  backdrop-filter: blur(15px);
}

.glass-modal .el-dialog__footer {
  background: rgba(17, 34, 64, 0.9);
  backdrop-filter: blur(15px);
  border-top: 1px solid rgba(100, 255, 218, 0.2);
}

.glass-modal .el-dialog__title {
  color: #e8e8e8;
}

.glass-modal .el-form-item__label {
  color: rgba(232, 232, 232, 0.8);
}

.glass-modal .el-input__wrapper {
  background: rgba(100, 255, 218, 0.05);
  border: 1px solid rgba(100, 255, 218, 0.2);
}

.glass-modal .el-input__input {
  color: #e8e8e8;
}

@media (max-width: 1200px) {
  .top-bar {
    flex-direction: column;
    gap: 20px;
    text-align: center;
    padding: 20px;
  }

  .user-info {
    justify-content: center;
  }

  .top-actions {
    justify-content: center;
    flex-wrap: wrap;
    gap: 15px;
  }
}

@media (max-width: 992px) {
  .left-nav {
    width: 60px;
    gap: 20px;
  }

  .nav-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 15px;
  }

  .action-buttons .el-button {
    width: 100%;
    min-width: unset;
  }
  
  .game-stats {
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .stat-item {
    font-size: 13px;
    padding: 6px 12px;
  }
}

@media (max-width: 768px) {
  .home-container {
    flex-direction: column;
  }

  .left-nav {
    width: 100%;
    height: auto;
    flex-direction: row;
    padding: 10px;
    gap: 10px;
    justify-content: center;
  }

  .main-content {
    padding: 10px;
  }

  .top-bar {
    padding: 10px;
  }

  .game-tabs {
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
  }
  
  .game-tab {
    padding: 10px 18px;
  }
  
  .tab-name {
    font-size: 14px;
  }
  
  .game-description {
    font-size: 15px;
  }
  
  .game-tips {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 5px;
  }

  .top-bar {
    padding: 12px;
    margin-bottom: 20px;
  }

  .game-tabs {
    flex-direction: column;
    width: 100%;
  }

  .game-tab {
    justify-content: center;
  }

  .game-actions {
    padding: 15px;
  }
  
  .user-avatar {
    width: 50px;
    height: 50px;
  }
  
  .username {
    font-size: 18px;
  }
  
  .user-stats {
    flex-wrap: wrap;
    gap: 8px;
    font-size: 12px;
  }
  
  .top-stats {
    flex-wrap: wrap;
    gap: 8px;
    font-size: 12px;
  }
  
  .game-title-section h2 {
    font-size: 1.5rem;
  }
}
</style>