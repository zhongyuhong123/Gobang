<template>
  <div class="home-container">
    <!-- 游戏模式选择 -->
    <div class="game-modes">
      <h2 class="section-title">选择游戏模式</h2>
      <div class="modes-grid">
        <div class="mode-card gobang" @click="selectGameMode('gobang')">
          <span class="mode-icon">⚫</span>
          <h3>五子棋</h3>
          <p>经典五子棋对战</p>
        </div>
        <div class="mode-card military" @click="selectGameMode('military')">
          <span class="mode-icon">♟️</span>
          <h3>军棋</h3>
          <p>策略军棋对决</p>
        </div>
        <div class="mode-card chinese-chess" @click="selectGameMode('chinese-chess')">
          <span class="mode-icon">♜</span>
          <h3>中国象棋</h3>
          <p>传统象棋对弈</p>
        </div>
      </div>
    </div>

    <!-- 游戏操作区域 -->
    <div class="game-actions">
      <div class="placeholder-text" v-if="!selectedGameMode">
        <div class="chess-icon">♔</div>
        <h3>请选择游戏模式开始</h3>
        <p>选择上方的游戏模式来开始您的对弈之旅</p>
      </div>
      
      <div v-else class="game-mode-content">
        <h3>{{ getGameModeTitle(selectedGameMode) }}</h3>
        <div class="mode-actions">
          <button class="btn btn-primary" @click="startQuickMatch">快速匹配</button>
          <button class="btn btn-secondary" @click="createRoom">创建房间</button>
          <button class="btn btn-secondary" @click="joinRoom">加入房间</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
    return {
      selectedGameMode: null
    }
  },
  methods: {
    selectGameMode(mode) {
      this.selectedGameMode = mode
    },
    getGameModeTitle(mode) {
      const titles = {
        'gobang': '五子棋',
        'military': '军棋',
        'chinese-chess': '中国象棋'
      }
      return titles[mode] || '未知游戏'
    },
    startQuickMatch() {
      if (!this.selectedGameMode) return
      this.$message.info('正在为您匹配对手...')
      // TODO: 调用后端匹配接口
    },
    createRoom() {
      if (!this.selectedGameMode) return
      this.$message.info('正在创建房间...')
      // TODO: 调用后端创建房间接口
    },
    joinRoom() {
      if (!this.selectedGameMode) return
      this.$prompt('请输入房间号', '加入房间', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      }).then(({ value }) => {
        this.$message.success(`正在加入房间 ${value}...`)
        // TODO: 调用后端加入房间接口
      }).catch(() => {
        this.$message.info('取消加入房间')
      })
    }
  }
}
</script>

<style scoped>
/* 游戏模式选择 */
.game-modes {
  margin-bottom: 40px;
}

.section-title {
  text-align: center;
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 30px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.modes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 25px;
  max-width: 800px;
  margin: 0 auto;
}

.mode-card {
  background: rgba(30, 41, 59, 0.9);
  border-radius: var(--radius-lg);
  padding: 30px 20px;
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 2px solid transparent;
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-lg);
}

.mode-card:hover {
  transform: translateY(-5px);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-xl);
}

.mode-card.gobang:hover {
  border-color: #000;
  background: rgba(0, 0, 0, 0.2);
}

.mode-card.military:hover {
  border-color: #4CAF50;
  background: rgba(76, 175, 80, 0.1);
}

.mode-card.chinese-chess:hover {
  border-color: #FF9800;
  background: rgba(255, 152, 0, 0.1);
}

.mode-icon {
  font-size: 3rem;
  margin-bottom: 15px;
  display: block;
}

.mode-card h3 {
  font-size: 1.3rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.mode-card p {
  color: var(--text-secondary);
  font-size: 14px;
}

/* 游戏操作区域 */
.game-actions {
  display: flex;
  gap: 30px;
  max-width: 1000px;
  width: 100%;
  justify-content: center;
  flex-wrap: wrap;
  animation: fadeIn 0.6s ease-out;
}

.placeholder-text {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.chess-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.6;
  animation: float 3s ease-in-out infinite;
}

.game-mode-content {
  text-align: center;
  padding: 40px;
  background: rgba(30, 41, 59, 0.8);
  border-radius: var(--radius-lg);
  backdrop-filter: blur(10px);
}

.game-mode-content h3 {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin-bottom: 30px;
}

.mode-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  min-width: 120px;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background: var(--primary-hover);
  transform: translateY(-2px);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.home-container {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>