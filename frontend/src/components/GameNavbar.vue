<template>
  <nav class="navbar">
    <div class="nav-container">
      <div class="nav-logo">
        <h1>{{ title }}</h1>
      </div>
      
      <div class="nav-actions">
        <button 
          v-if="showBackButton"
          class="liquid-btn back-btn" 
          @click="$router.push('/')"
          aria-label="返回首页">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
        </button>
        
        <template v-if="!isLogin || forceShowAuthBtns">
          <button @click="$router.push('/login')" class="liquid-btn login-btn">
            <span class="btn-text">登录</span>
          </button>
        </template>
        
        <template v-else-if="isLogin && !forceShowAuthBtns">
          <button @click="$router.push('/game')" class="liquid-btn enter-game-btn">
            <span class="btn-text">进入游戏</span>
          </button>
          <button @click="handleLogout" class="liquid-btn logout-btn">
            <span class="btn-text">退出登录</span>
          </button>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

defineProps({
  title: {
    type: String,
    default: '五子棋对战'
  },
  showBackButton: {
    type: Boolean,
    default: true
  },
  showLoginHint: {
    type: Boolean,
    default: false
  },
  forceShowAuthBtns: {
    type: Boolean,
    default: false
  }
})

const isLogin = ref(false)

const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLogin.value = !!token
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  isLogin.value = false
  ElMessage.success('退出登录成功')
  router.push('/')
}

onMounted(() => {
  checkLoginStatus()
})
</script>

<style scoped>
.navbar {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.18);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10000;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  transition: all 0.3s ease;
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
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  cursor: pointer;
  transition: transform 0.3s ease, color 0.3s ease;
  letter-spacing: -1px;
  background: linear-gradient(135deg, var(--primary-color), var(--text-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  user-select: none;
  outline: none;
  -webkit-user-select: none;
}

.nav-logo h1:hover {
  transform: scale(1.02);
  color: var(--primary-color);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.liquid-btn {
  position: relative;
  border: none;
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 6px 24px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    inset 0 -1px 0 rgba(255, 255, 255, 0.1);
  min-width: 80px;
  text-align: center;
  user-select: none;
  outline: none;
  -webkit-user-select: none;
}

.btn-text {
  position: relative;
  display: block;
  transition: all 0.3s ease;
  z-index: 2;
}



.login-btn {
  color: #007AFF;
  background: linear-gradient(135deg, 
    rgba(0, 122, 255, 0.15) 0%, 
    rgba(255, 255, 255, 0.05) 50%, 
    rgba(0, 122, 255, 0.1) 100%);
}

.login-btn:hover {
  transform: translateY(-1px) scale(1.01);
  box-shadow: 
    0 8px 28px rgba(0, 122, 255, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.4),
    inset 0 -1px 0 rgba(255, 255, 255, 0.2);
  background: linear-gradient(135deg, 
    rgba(0, 122, 255, 0.2) 0%, 
    rgba(255, 255, 255, 0.1) 50%, 
    rgba(0, 122, 255, 0.15) 100%);
}

.login-btn:active {
  transform: translateY(0) scale(0.98);
}

.enter-game-btn {
  color: #34C759;
  background: linear-gradient(135deg, 
    rgba(52, 199, 89, 0.15) 0%, 
    rgba(255, 255, 255, 0.05) 50%, 
    rgba(52, 199, 89, 0.1) 100%);
}

.enter-game-btn:hover {
  transform: translateY(-1px) scale(1.01);
  box-shadow: 
    0 8px 28px rgba(52, 199, 89, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.4),
    inset 0 -1px 0 rgba(255, 255, 255, 0.2);
  background: linear-gradient(135deg, 
    rgba(52, 199, 89, 0.2) 0%, 
    rgba(255, 255, 255, 0.1) 50%, 
    rgba(52, 199, 89, 0.15) 100%);
}

.enter-game-btn:active {
  transform: translateY(0) scale(0.98);
}

.logout-btn {
  color: #FF3B30;
  background: linear-gradient(135deg, 
    rgba(255, 59, 48, 0.15) 0%, 
    rgba(255, 255, 255, 0.05) 50%, 
    rgba(255, 59, 48, 0.1) 100%);
}

.logout-btn:hover {
  transform: translateY(-1px) scale(1.01);
  box-shadow: 
    0 8px 28px rgba(255, 59, 48, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.4),
    inset 0 -1px 0 rgba(255, 255, 255, 0.2);
  background: linear-gradient(135deg, 
    rgba(255, 59, 48, 0.2) 0%, 
    rgba(255, 255, 255, 0.1) 50%, 
    rgba(255, 59, 48, 0.15) 100%);
}

.logout-btn:active {
  transform: translateY(0) scale(0.98);
}

.back-btn {
  color: #8E8E93;
  background: linear-gradient(135deg, 
    rgba(142, 142, 147, 0.15) 0%, 
    rgba(255, 255, 255, 0.05) 50%, 
    rgba(142, 142, 147, 0.1) 100%);
  padding: 8px 12px;
  min-width: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  -webkit-user-select: none;
}

.back-btn:hover {
  transform: translateY(-1px) scale(1.01);
  box-shadow: 
    0 8px 28px rgba(142, 142, 147, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.4),
    inset 0 -1px 0 rgba(255, 255, 255, 0.2);
  background: linear-gradient(135deg, 
    rgba(142, 142, 147, 0.2) 0%, 
    rgba(255, 255, 255, 0.1) 50%, 
    rgba(142, 142, 147, 0.15) 100%);
  color: #007AFF;
}

.back-btn:active {
  transform: translateY(0) scale(0.98);
}

.back-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
  user-select: none;
  -webkit-user-select: none;
}

.back-btn:hover .back-icon {
  transform: translateX(-2px);
}

.liquid-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.2), 
    transparent);
  transition: left 0.6s ease;
}

.liquid-btn:hover::before {
  left: 100%;
}

@keyframes subtle-pulse {
  0% {
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }
  50% {
    box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15), inset 0 1px 0 rgba(255, 255, 255, 0.4);
  }
  100% {
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }
}

.liquid-btn {
  animation: subtle-pulse 4s ease-in-out infinite;
}

.liquid-btn:hover {
  animation: none;
}



@media (max-width: 768px) {
  .nav-container {
    padding: 0 10px;
  }
  
  .nav-logo h1 {
    font-size: 24px;
  }
  
  .nav-actions {
    gap: 8px;
  }
  
  .liquid-btn {
    padding: 6px 12px;
  font-size: 12px;
  min-width: 70px;
  -webkit-user-select: none;
}
  
  .back-btn {
    padding: 6px 10px;
    min-width: auto;
  }
  
  .back-icon {
    font-size: 14px;
  }
}
</style>
