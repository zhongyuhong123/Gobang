<template>
  <div class="welcome-container">
    <!-- 通用导航栏 -->
    <GameNavbar />
    
    <!-- 导航栏占位符，确保内容不会被导航栏遮挡 -->
    <div class="navbar-placeholder"></div>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="hero-section">
        <div class="hero-content">
          <h2 class="hero-title animate-on-scroll">经典五子棋对战</h2>
          <p class="hero-subtitle animate-on-scroll">体验传统棋艺的智慧与乐趣</p>
          <div class="hero-features">
            <div class="feature-item animate-on-scroll">
              <div class="feature-icon">⚫</div>
              <h4>经典玩法</h4>
              <p>黑白对弈，连五取胜</p>
            </div>
            <div class="feature-item animate-on-scroll">
              <div class="feature-icon">👥</div>
              <h4>在线对战</h4>
              <p>与全球玩家实时竞技</p>
            </div>
            <div class="feature-item animate-on-scroll">
              <div class="feature-icon">🏆</div>
              <h4>段位系统</h4>
              <p>挑战自我，提升棋艺</p>
            </div>
          </div>
        </div>
        <div class="hero-visual animate-on-scroll">
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
          <h3 class="animate-on-scroll">游戏规则</h3>
          <div class="rules-content">
            <div class="rule-item animate-on-scroll">
              <h4>基本规则</h4>
              <p>黑白双方轮流在棋盘上放置棋子，先连成五子（横、竖、斜）的一方获胜。</p>
            </div>
            <div class="rule-item animate-on-scroll">
              <h4>禁手规则</h4>
              <p>黑棋有禁手限制，包括三三、四四、长连等，白棋无禁手。</p>
            </div>
            <div class="rule-item animate-on-scroll">
              <h4>胜负判定</h4>
              <p>连成五子即获胜，若棋盘填满仍未分出胜负则为和棋。</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 底部行动按钮 -->
      <section class="action-section animate-on-scroll">
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
import { onMounted, onUnmounted } from 'vue'
import GameNavbar from '../components/GameNavbar.vue'

export default {
  name: 'WelcomeView',
  components: {
    GameNavbar
  },
  setup() {
    const router = useRouter()
    let observer = null

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

    onMounted(() => {
      // 等待DOM渲染完成后初始化IntersectionObserver
      setTimeout(() => {
        // 创建观察者实例
        observer = new IntersectionObserver((entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              // 元素进入视口，添加active类
              entry.target.classList.add('active')
            } else {
              // 元素离开视口，移除active类
              entry.target.classList.remove('active')
            }
          })
        }, {
          threshold: 0.1, // 当元素10%进入视口时触发
          rootMargin: '0px 0px -50px 0px' // 底部偏移，提前50px触发
        })

        // 观察所有带有animate-on-scroll类的元素
        const animatedElements = document.querySelectorAll('.animate-on-scroll')
        animatedElements.forEach(el => observer.observe(el))
      }, 100)
    })

    onUnmounted(() => {
      // 清理观察者
      if (observer) {
        observer.disconnect()
      }
    })

    return {
      handleStartGame
    }
  }
}
</script>

<style scoped>
/* 滚动动画效果 */
.animate-on-scroll {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
  transition: opacity 0.8s ease, transform 0.8s ease;
  will-change: opacity, transform;
}

.animate-on-scroll.active {
  opacity: 1;
  transform: scale(1) translateY(0);
}
.welcome-container {
  min-height: 100vh;
  background: var(--color-background-primary);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.navbar-placeholder {
  height: 70px; /* 与导航栏高度保持一致 */
}

/* 主要内容样式 */
.main-content {
  padding-top: 0;
}

.hero-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  animation: fadeInUp 0.8s ease-out;
}

.hero-title {
  font-size: 3.2rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 1.5rem;
  line-height: 1.2;
  letter-spacing: -0.3px;
}

.hero-subtitle {
  font-size: 1.2rem;
  color: var(--color-text-secondary);
  margin-bottom: 3.5rem;
  line-height: 1.6;
  max-width: 500px;
}

.hero-features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.feature-item {
  text-align: center;
  padding: 1.5rem;
  background: var(--color-surface);
  border-radius: var(--radius-medium);
  border: 1px solid var(--color-border);
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.feature-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.feature-icon {
  font-size: 2.2rem;
  margin-bottom: 1rem;
}

.feature-item h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 0.5rem;
}

.feature-item p {
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  line-height: 1.4;
}

/* 棋盘预览样式 */
.chessboard-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  animation: scaleIn 0.8s ease-out 0.3s both;
}

.board-grid {
  display: grid;
  grid-template-rows: repeat(15, 20px);
  grid-template-columns: repeat(15, 20px);
  background: #deb887;
  padding: 10px;
  border-radius: var(--radius-small);
  box-shadow: var(--shadow-md);
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
  background: var(--color-text-primary);
  box-shadow: 0 2px 4px rgb(0, 0, 0);
}

.white-piece::before {
  background: var(--color-surface);
  box-shadow: 0 2px 4px rgb(0, 0, 0);
}

/* 规则介绍样式 */
.rules-section {
  background: var(--color-background-secondary);
  padding: 80px 0;
  animation: fadeInUp 0.8s ease-out 0.5s both;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.rules-section h3 {
  text-align: center;
  font-size: 2.2rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 3rem;
}

.rules-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2.5rem;
}

.rule-item {
  text-align: center;
  padding: 2rem;
  background: var(--color-surface);
  border-radius: var(--radius-medium);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
}

.rule-item:hover {
  box-shadow: var(--shadow-md);
}

.rule-item h4 {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 1rem;
}

.rule-item p {
  color: var(--color-text-secondary);
  line-height: 1.6;
}

/* 行动区域样式 */
.action-section {
  padding: 100px 0;
  text-align: center;
  background: var(--color-surface);
  margin: 0 20px;
  border-radius: var(--radius-large);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  animation: fadeInUp 0.8s ease-out 0.7s both;
}

.action-section h3 {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 1.5rem;
}

.action-section p {
  font-size: 1.2rem;
  color: var(--color-text-secondary);
  margin-bottom: 3.5rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.action-buttons {
  display: flex;
  gap: 2rem;
  justify-content: center;
}

.action-btn {
  padding: 12px 32px;
  border: none;
  border-radius: var(--radius-small);
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: inherit;
}

.action-btn.primary {
  background: var(--color-primary);
  color: white;
  box-shadow: var(--shadow-sm);
}

.action-btn.primary:hover {
  background: var(--color-primary-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.action-btn.secondary {
  background: var(--color-surface);
  color: var(--color-primary);
  border: 1px solid var(--color-border);
}

.action-btn.secondary:hover {
  background: var(--color-background-hover);
  border-color: var(--color-primary);
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section {
    grid-template-columns: 1fr;
    gap: 40px;
    padding: 60px 20px;
  }
  
  .hero-title {
    font-size: 2.2rem;
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