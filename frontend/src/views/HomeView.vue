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
            <el-avatar :size="48" :style="{ backgroundColor: getAvatarColor(userInfo.username) }">
              {{ getUsernameInitial(userInfo.username) }}
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
          <div class="guest-actions" v-if="!userInfo">
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
          <div class="game-tab" :class="{ active: selectedGameMode === 'gobang' }" @click="selectGameMode('gobang')">
            <span class="tab-icon">
              <Check />
            </span>
            <span class="tab-name">五子棋</span>
          </div>

          <div class="game-tab" :class="{ active: selectedGameMode === 'military' }"
            @click="selectGameMode('military')">
            <span class="tab-icon">
              <Flag />
            </span>
            <span class="tab-name">军棋</span>
          </div>

          <div class="game-tab" :class="{ active: selectedGameMode === 'chinese-chess' }"
            @click="selectGameMode('chinese-chess')">
            <span class="tab-icon">
              <PieChart />
            </span>
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
          <button class="hero-button hero-button-primary" @click="startQuickMatch"
            :disabled="matchingLoading || !userInfo">
            <div class="button-content">
              <span class="button-title">快速匹配</span>
              <span class="button-subtitle">立即开始对战</span>
            </div>
            <div class="button-glow"></div>
          </button>

          <button class="hero-button hero-button-success" @click="createRoom" :disabled="creatingRoom || !userInfo">
            <div class="button-content">
              <span class="button-title">创建房间</span>
              <span class="button-subtitle">邀请好友对战</span>
            </div>
            <div class="button-glow"></div>
          </button>
        </div>
      </div>
      
      <!-- 匹配状态显示 -->
      <div class="matching-status" v-if="isMatching">
        <div class="matching-content">
          <div class="matching-spinner"></div>
          <div class="matching-text">
            <div class="matching-title">匹配中</div>
            <div class="matching-time" :class="{ warning: matchingElapsedTime >= 150 }">已等待 {{ formatMatchingTime(matchingElapsedTime) }}</div>
          </div>
          <button class="cancel-match-btn" @click="stopMatchingTimer(); matchingLoading = false;">
            取消匹配
          </button>
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
.nav-icon svg {
  transition: transform 0.3s ease;
  width: 18px;
  height: 18px;
}

.nav-icon:hover svg {
  transform: scale(1.15);
}

.nav-icon:active {
  transform: scale(0.95);
  background: rgba(100, 255, 218, 0.15);
}

.user-avatar {
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
}

/* 匹配状态样式 */
.matching-status {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(17, 34, 64, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(100, 255, 218, 0.3);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  z-index: 1000;
  min-width: 300px;
}

.matching-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.matching-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(100, 255, 218, 0.2);
  border-top: 3px solid rgba(100, 255, 218, 0.8);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.matching-text {
  text-align: center;
  color: #e8e8e8;
}

.matching-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: rgba(100, 255, 218, 0.9);
}

.matching-time {
  font-size: 16px;
  color: rgba(232, 232, 232, 0.8);
  font-weight: 500;
  transition: color 0.3s ease;
}

.matching-time.warning {
  color: #ffb74d;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.6; }
  100% { opacity: 1; }
}

.cancel-match-btn {
  background: rgba(255, 77, 77, 0.2);
  border: 1px solid rgba(255, 77, 77, 0.4);
  color: #ff4d4d;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-match-btn:hover {
  background: rgba(255, 77, 77, 0.3);
  border-color: rgba(255, 77, 77, 0.6);
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
    const isMatching = ref(false)
    const matchingElapsedTime = ref(0)
    let matchingTimer = null

    const userInfo = ref(null)

    const gameStats = reactive({
      gobang: null,
      military: null,
      chineseChess: null
    })

    const totalOnline = ref(0)
    const totalGames = ref(0)

    const updateTotalStats = () => {
      const online = (gameStats.gobang?.online || 0) +
        (gameStats.military?.online || 0) +
        (gameStats.chineseChess?.online || 0)
      const games = (gameStats.gobang?.activeGames || 0) +
        (gameStats.military?.activeGames || 0) +
        (gameStats.chineseChess?.activeGames || 0)
      totalOnline.value = online
      totalGames.value = games
    }

    const settingsDialog = reactive({
      visible: false
    })

    const loadUserInfo = async () => {
      try {
        const token = localStorage.getItem('token')
        if (!token) return

        const savedUserInfo = localStorage.getItem('userInfo')
        if (savedUserInfo) userInfo.value = JSON.parse(savedUserInfo)

        const response = await userAPI.getUserInfo()
        if (response.success && response.data) {
          userInfo.value = response.data
          localStorage.setItem('userInfo', JSON.stringify(response.data))
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
      const ranks = ['新手', '初级', '中级', '高级', '专家', '大师', '宗师']
      return ranks[rank] || '新手'
    }

    const getWinRate = (user) => {
      if (!user || !user.totalGames) return '0%'
      return Math.round((user.winGames || 0) / user.totalGames * 100) + '%'
    }

    const getGameStats = (mode) => gameStats[mode === 'chinese-chess' ? 'chineseChess' : mode] || null

    const getUsernameInitial = (username) => {
      return username ? username.charAt(0).toUpperCase() : '用'
    }

    const getAvatarColor = (username) => {
      if (!username) return '#64ffda'

      const colors = [
        '#64ffda', '#4fc3f7', '#81c784', '#ffb74d', '#ff8a65',
        '#ba68c8', '#4db6ac', '#7986cb', '#a1887f', '#90a4ae'
      ]

      let hash = 0
      for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash)
      }

      return colors[Math.abs(hash) % colors.length]
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

    const formatMatchingTime = (seconds) => {
      if (seconds < 60) {
        return `${seconds}秒`
      } else {
        const minutes = Math.floor(seconds / 60)
        const remainingSeconds = seconds % 60
        return `${minutes}分${remainingSeconds}秒`
      }
    }

    const startMatchingTimer = () => {
      matchingElapsedTime.value = 0
      isMatching.value = true
      matchingTimer = setInterval(() => {
        matchingElapsedTime.value++
        
        // 检查是否超过3分钟（180秒）
        if (matchingElapsedTime.value >= 180) {
          stopMatchingTimer()
          matchingLoading.value = false
          ElMessage.error('匹配失败')
        }
      }, 1000)
    }

    const stopMatchingTimer = () => {
      if (matchingTimer) {
        clearInterval(matchingTimer)
        matchingTimer = null
      }
      isMatching.value = false
      matchingElapsedTime.value = 0
    }

    const startQuickMatch = async () => {
      if (!userInfo.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }

      matchingLoading.value = true
      startMatchingTimer()
      
      try {
        const response = await gameAPI.quickMatch(selectedGameMode.value)
        if (response.success) {
          if (response.data && response.data.gameId) {
            stopMatchingTimer()
            ElMessage.success('匹配成功')
            router.push(`/game/${response.data.gameId}`)
          } else {
            ElMessage.info(response.message || '正在匹配')
          }
        } else {
          stopMatchingTimer()
          ElMessage.error(response.message || '匹配失败')
        }
      } catch (error) {
        stopMatchingTimer()
        console.error('快速匹配失败:', error)
        ElMessage.error('匹配失败，请重试')
      } finally {
        if (!isMatching.value) {
          matchingLoading.value = false
        }
      }
    }

    const createRoom = async () => {
      if (!userInfo.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }

      creatingRoom.value = true
      try {
        const response = await gameAPI.createRoom(selectedGameMode.value)
        if (response.success) {
          if (response.data && response.data.roomId) {
            ElMessage.success('房间创建成功')
            router.push(`/room/${response.data.roomId}`)
          } else {
            ElMessage.error('创建房间响应数据异常')
          }
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
      isMatching,
      matchingElapsedTime,
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
      getUsernameInitial,
      getAvatarColor,
      startQuickMatch,
      createRoom,
      goToHome,
      goToFriends,
      goToRanking,
      showSettings,
      stopMatchingTimer,
      formatMatchingTime
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
  width: 60px;
  background: rgba(17, 34, 64, 0.95);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(100, 255, 218, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20px;
  gap: 20px;
}

.nav-icon {
  width: 36px;
  height: 36px;
  background: rgba(100, 255, 218, 0.05);
  border: 1px solid rgba(100, 255, 218, 0.15);
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: rgba(100, 255, 218, 0.6);
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.nav-icon:hover {
  background: rgba(100, 255, 218, 0.1);
  transform: translateY(-2px);
  color: rgba(100, 255, 218, 0.9);
  border-color: rgba(100, 255, 218, 0.3);
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
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 18px;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
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
  transition: all 0.3s ease;
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
}

.game-tab.active {
  background: rgba(100, 255, 218, 0.15);
  border: 1px solid rgba(100, 255, 218, 0.4);
  color: rgba(100, 255, 218, 1);
  box-shadow: 0 0 20px rgba(100, 255, 218, 0.25);
  transform: scale(1.05);
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
  justify-content: center;
}



.hero-button {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 48px;
  border: none;
  border-radius: 50px 15px 50px 15px;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.05em;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  min-width: 220px;
  transform: perspective(1000px) rotateX(5deg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.hero-button-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: 2px solid rgba(102, 126, 234, 0.5);
}

.hero-button-success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
  border: 2px solid rgba(17, 153, 142, 0.5);
  border-radius: 15px 50px 15px 50px;
}

.hero-button:hover {
  transform: perspective(1000px) rotateX(0deg) translateY(-3px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
}

.hero-button-primary:hover {
  background: linear-gradient(135deg, #7c8ff0 0%, #8a5fb8 100%);
}

.hero-button-success:hover {
  background: linear-gradient(135deg, #13b2a7 0%, #4cf996 100%);
}

.hero-button:active {
  transform: perspective(1000px) rotateX(5deg) translateY(0px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.hero-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: perspective(1000px) rotateX(5deg);
}

.button-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.button-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.button-subtitle {
  font-size: 13px;
  opacity: 0.8;
  font-weight: 400;
  letter-spacing: 0.3px;
}

.button-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.2) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.hero-button:hover .button-glow {
  opacity: 0.8;
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