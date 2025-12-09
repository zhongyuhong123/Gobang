<template>
  <div class="welcome-container">
    <div class="bg-decoration"></div>
    <GameNavbar />
    <div class="navbar-placeholder"></div>
    <main class="main-content">
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title animate-on-scroll">经典五子棋对战</h1>
          <p class="hero-subtitle animate-on-scroll">体验传统棋艺的智慧与乐趣</p>
          <div class="hero-features">
            <div class="feature-item animate-on-scroll glass-effect" style="--delay: 0s;">
              <h4>经典玩法</h4>
              <p>黑白对弈，连五取胜</p>
            </div>
            <div class="feature-item animate-on-scroll glass-effect" style="--delay: 0.2s;">
              <h4>在线对战</h4>
              <p>与全球玩家实时竞技</p>
            </div>
            <div class="feature-item animate-on-scroll glass-effect" style="--delay: 0.4s;">
              <h4>段位系统</h4>
              <p>挑战自我，提升棋艺</p>
            </div>
          </div>
        </div>
        <div class="hero-visual animate-on-scroll">
          <div class="chessboard-preview">
            <div class="board-grid">
              <div v-for="i in 15" :key="i" class="board-row">
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
      <section class="rules-section">
        <div class="container">
          <h2 class="section-title animate-on-scroll">游戏规则</h2>
          <div class="rules-content">
            <div class="rule-item animate-on-scroll" style="--delay: 0s;">
              <h3>基本规则</h3>
              <p>黑白双方轮流在棋盘上放置棋子，先连成五子（横、竖、斜）的一方获胜。</p>
            </div>
            <div class="rule-item animate-on-scroll" style="--delay: 0.2s;">
              <h3>禁手规则</h3>
              <p>黑棋有禁手限制，包括三三、四四、长连等，白棋无禁手。</p>
            </div>
            <div class="rule-item animate-on-scroll" style="--delay: 0.4s;">
              <h3>胜负判定</h3>
              <p>连成五子即获胜，若棋盘填满仍未分出胜负则为和棋。</p>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import { onMounted, onUnmounted } from 'vue'
import GameNavbar from '../components/GameNavbar.vue'

export default {
  name: 'WelcomeView',
  components: {
    GameNavbar
  },
  setup() {
    let observer = null

    onMounted(() => {
      setTimeout(() => {
        observer = new IntersectionObserver((entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              entry.target.classList.add('active')
            }
          })
        }, {
          threshold: 0.05,
          rootMargin: '0px 0px -150px 0px'
        })

        const animatedElements = document.querySelectorAll('.animate-on-scroll')
        animatedElements.forEach(el => observer.observe(el))
      }, 100)

      const blackPiece = document.querySelector('.black-piece')
      const whitePiece = document.querySelector('.white-piece')
      
      if (blackPiece && whitePiece) {
        setTimeout(() => {
          blackPiece.classList.add('pulse')
        }, 1500)
        
        setTimeout(() => {
          whitePiece.classList.add('pulse')
        }, 2000)
      }
    })

    onUnmounted(() => {
      if (observer) {
        observer.disconnect()
      }
    })

    return {}
  }
}
</script>

<style scoped>
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 10% 20%, rgba(0, 113, 227, 0.08) 0%, transparent 30%),
              radial-gradient(circle at 90% 80%, rgba(52, 199, 89, 0.08) 0%, transparent 30%),
              radial-gradient(circle at 50% 50%, rgba(255, 149, 0, 0.05) 0%, transparent 40%);
  pointer-events: none;
  z-index: 0;
}

.animate-on-scroll {
  opacity: 0;
  transform: scale(0.9) translateY(30px);
  transition: opacity 0.8s cubic-bezier(0.4, 0, 0.2, 1), 
              transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: var(--delay, 0s);
  will-change: opacity, transform;
}

.animate-on-scroll.active {
  opacity: 1;
  transform: scale(1) translateY(0);
}

.welcome-container {
  min-height: 100vh;
  background: var(--bg-primary);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  position: relative;
  overflow-x: hidden;
}

.navbar-placeholder {
  height: 70px;
}

.main-content {
  padding-top: 0;
  position: relative;
  z-index: 1;
}

.hero-section {
  max-width: 1280px;
  margin: 0 auto;
  padding: 120px 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 100px;
  align-items: center;
}

.hero-title {
  font-size: 4rem;
  font-weight: 900;
  color: var(--text-primary);
  margin-bottom: 1.8rem;
  line-height: 1.05;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, var(--text-primary), #333);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.hero-subtitle {
  font-size: 1.4rem;
  color: var(--text-secondary);
  margin-bottom: 4rem;
  line-height: 1.8;
  max-width: 560px;
  letter-spacing: -0.01em;
}

.hero-features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.feature-item {
  text-align: center;
  padding: 2.2rem 1.8rem;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.feature-item.glass-effect {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(25px);
  -webkit-backdrop-filter: blur(25px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 10px 40px 0 rgba(31, 38, 135, 0.3);
  border-radius: var(--radius-lg);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-item:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  background: rgba(255, 255, 255, 0.35);
  border-color: rgba(255, 255, 255, 0.3);
}

.feature-icon {
  font-size: 2.8rem;
  margin-bottom: 1.2rem;
  display: block;
  transition: transform 0.3s ease;
}

.feature-item:hover .feature-icon {
  transform: scale(1.1);
}

.feature-item h4 {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.8rem;
}

.feature-item p {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.6;
}

.hero-visual {
  position: relative;
}

.chessboard-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite;
  transform: perspective(1000px) rotateY(10deg);
  transition: transform 0.6s ease;
  padding: 20px;
}

.chessboard-preview:hover {
  transform: perspective(1000px) rotateY(0deg) scale(1.02);
}

.board-grid {
  display: grid;
  grid-template-rows: repeat(15, 22px);
  grid-template-columns: repeat(15, 22px);
  background: var(--board-color);
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.2),
              inset 0 2px 10px rgba(0, 0, 0, 0.1);
  border: 3px solid var(--board-border);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.board-grid::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.2) 0%, transparent 50%);
  pointer-events: none;
}

.board-row {
  display: contents;
}

.board-cell {
  width: 22px;
  height: 22px;
  position: relative;
  border: 1px solid rgba(139, 69, 19, 0.3);
}

.board-cell:nth-child(8):nth-of-type(8)::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 4px;
  height: 4px;
  background: var(--board-line);
  border-radius: 50%;
  z-index: 0;
}

.board-cell::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 18px;
  height: 18px;
  border-radius: 50%;
  z-index: 1;
  transition: all 0.3s ease;
}

.black-piece::before {
  background: linear-gradient(145deg, #000000, #333333);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4),
              inset 0 2px 4px rgba(255, 255, 255, 0.1);
  border: 1px solid #111;
  animation: pieceAppear 0.6s ease-out;
}

.white-piece::before {
  background: linear-gradient(145deg, #ffffff, #e0e0e0);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2),
              inset 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
  animation: pieceAppear 0.6s ease-out 0.3s both;
}

.black-piece.pulse::before,
.white-piece.pulse::before {
  animation: pulse 2s ease-in-out infinite;
}

.rules-section {
  background: linear-gradient(135deg, var(--bg-secondary), rgba(242, 242, 247, 0.8));
  padding: 100px 0;
  position: relative;
  overflow: hidden;
}

.rules-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-light), transparent);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 3rem;
  position: relative;
  display: inline-block;
  left: 50%;
  transform: translateX(-50%);
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-color), var(--success-color));
  border-radius: 3px;
}

.rules-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2.5rem;
}

.rule-item {
  text-align: center;
  padding: 3rem 2.5rem;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  border-radius: var(--radius-lg);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  min-height: 240px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1rem;
}

.rule-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at top right, rgba(0, 113, 227, 0.05), transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.rule-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 15px 40px rgba(31, 38, 135, 0.25);
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.25);
}

.rule-item:hover::before {
  opacity: 1;
}

.rule-icon {
  font-size: 3rem;
  margin-bottom: 1.5rem;
  display: block;
  transition: transform 0.3s ease;
}

.rule-item:hover .rule-icon {
  transform: scale(1.1) rotate(5deg);
}

.rule-item h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1.2rem;
  position: relative;
  z-index: 1;
}

.rule-item p {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 1.05rem;
  position: relative;
  z-index: 1;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.85);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-10px) rotate(1deg);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4),
                inset 0 2px 4px rgba(255, 255, 255, 0.1);
  }
  50% {
    transform: translate(-50%, -50%) scale(1.1);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.5),
                inset 0 2px 4px rgba(255, 255, 255, 0.1);
  }
}

@keyframes pieceAppear {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.5);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 992px) {
  .hero-section {
    grid-template-columns: 1fr;
    gap: 60px;
    padding: 80px 20px;
    text-align: center;
  }
  
  .hero-title {
    font-size: 2.8rem;
    margin-left: auto;
    margin-right: auto;
  }
  
  .hero-subtitle {
    margin-left: auto;
    margin-right: auto;
  }
  
  .hero-features {
    grid-template-columns: repeat(3, 1fr);
    gap: 1.2rem;
  }
  
  .rules-content {
    grid-template-columns: repeat(3, 1fr);
    gap: 2rem;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 60px 20px;
    gap: 50px;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-features {
    grid-template-columns: 1fr;
    gap: 1.5rem;
    max-width: 400px;
    margin: 0 auto;
  }
  
  .rules-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  .board-grid {
    grid-template-rows: repeat(15, 18px);
    grid-template-columns: repeat(15, 18px);
    padding: 15px;
  }
  
  .board-cell {
    width: 18px;
    height: 18px;
  }
  
  .board-cell::before {
    width: 14px;
    height: 14px;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-subtitle {
    font-size: 1.1rem;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .rule-item {
    padding: 2rem 1.5rem;
  }
}
</style>