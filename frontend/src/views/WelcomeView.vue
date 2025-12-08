<template>
  <div class="welcome-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-container">
        <div class="nav-logo">
          <h1>五子棋</h1>
        </div>
        <div class="nav-actions">
          <button class="start-game-btn" @click="handleStartGame">
            开始游戏
          </button>
        </div>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="hero-section">
        <div class="hero-content">
          <h2 class="hero-title">经典五子棋对战</h2>
          <p class="hero-subtitle">体验传统棋艺的智慧与乐趣</p>
          <div class="hero-features">
            <div class="feature-item">
              <div class="feature-icon">⚫</div>
              <h4>经典玩法</h4>
              <p>黑白对弈，连五取胜</p>
            </div>
            <div class="feature-item">
              <div class="feature-icon">👥</div>
              <h4>在线对战</h4>
              <p>与全球玩家实时竞技</p>
            </div>
            <div class="feature-item">
              <div class="feature-icon">🏆</div>
              <h4>段位系统</h4>
              <p>挑战自我，提升棋艺</p>
            </div>
          </div>
        </div>
        <div class="hero-visual">
          <div class="chessboard-preview">
            <div class="board-grid">
              <div 
                v-for="i in 15" 
                :key="i" 
                class="board-row"
              >
                <div 
                  v-for="j in 15" 
                  :key="j" 
                  class="board-cell"
                  :class="{ 
                    'black-piece': i === 8 && j === 8,
                    'white-piece': i === 7 && j === 8
                  }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 游戏规则介绍 -->
      <section class="rules-section">
        <div class="container">
          <h3>游戏规则</h3>
          <div class="rules-content">
            <div class="rule-item">
              <h4>基本规则</h4>
              <p>黑白双方轮流在棋盘上放置棋子，先连成五子（横、竖、斜）的一方获胜。</p>
            </div>
            <div class="rule-item">
              <h4>禁手规则</h4>
              <p>黑棋有禁手限制，包括三三、四四、长连等，白棋无禁手。</p>
            </div>
            <div class="rule-item">
              <h4>胜负判定</h4>
              <p>连成五子即获胜，若棋盘填满仍未分出胜负则为和棋。</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 底部行动按钮 -->
      <section class="action-section">
        <div class="container">
          <h3>立即开始游戏</h3>
          <p>选择您喜欢的游戏模式，开始您的五子棋之旅</p>
          <div class="action-buttons">
            <button class="action-btn primary" @click="handleStartGame">
              开始游戏
            </button>
            <button class="action-btn secondary" @click="$router.push('/login')">
              登录账户
            </button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'WelcomeView',
  setup() {
    const router = useRouter()

    const handleStartGame = () => {
      // 检查用户是否已登录
      const token = localStorage.getItem('token')
      if (token) {
        // 已登录，跳转到游戏大厅
        router.push('/home')
      } else {
        // 未登录，跳转到登录页面
        router.push('/login')
      }
    }

    return {
      handleStartGame
    }
  }
}
</script>

<style scoped>
.welcome-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 导航栏样式 */
.navbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.nav-logo h1 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.start-game-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.start-game-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 主要内容样式 */
.main-content {
  padding-top: 70px;
}

.hero-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1rem;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 1.2rem;
  color: #7f8c8d;
  margin-bottom: 3rem;
  line-height: 1.6;
}

.hero-features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
}

.feature-item {
  text-align: center;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.feature-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.feature-item h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.feature-item p {
  color: #7f8c8d;
  font-size: 0.9rem;
  line-height: 1.4;
}

/* 棋盘预览样式 */
.chessboard-preview {
  display: flex;
  justify-content: center;
  align-items: center;
}

.board-grid {
  display: grid;
  grid-template-rows: repeat(15, 20px);
  grid-template-columns: repeat(15, 20px);
  background: #deb887;
  padding: 10px;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.board-row {
  display: contents;
}

.board-cell {
  width: 20px;
  height: 20px;
  position: relative;
  border: 1px solid #a0522d;
}

.board-cell::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 16px;
  border-radius: 50%;
  z-index: 1;
}

.black-piece::before {
  background: #2c3e50;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.white-piece::before {
  background: #ecf0f1;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* 规则介绍样式 */
.rules-section {
  background: rgba(255, 255, 255, 0.6);
  padding: 80px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.rules-section h3 {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 3rem;
}

.rules-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 3rem;
}

.rule-item {
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.rule-item h4 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.rule-item p {
  color: #7f8c8d;
  line-height: 1.6;
}

/* 行动区域样式 */
.action-section {
  padding: 80px 0;
  text-align: center;
}

.action-section h3 {
  font-size: 2.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.action-section p {
  font-size: 1.1rem;
  color: #7f8c8d;
  margin-bottom: 3rem;
}

.action-buttons {
  display: flex;
  gap: 2rem;
  justify-content: center;
}

.action-btn {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.action-btn.secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.action-btn.secondary:hover {
  background: #667eea;
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section {
    grid-template-columns: 1fr;
    gap: 40px;
    padding: 60px 20px;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-features {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .rules-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .action-btn {
    width: 200px;
  }
}
</style>