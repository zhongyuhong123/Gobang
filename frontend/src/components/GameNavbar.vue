<template>
  <nav class="navbar">
    <div class="nav-container">
      <!-- Logo和标题 -->
      <div class="nav-logo">
        <h1>{{ title }}</h1>
      </div>
      
      <!-- 导航按钮区域 -->
      <div class="nav-actions">
        <!-- 仅在非首页显示返回按钮 -->
        <button 
          v-if="showBackButton"
          class="back-btn" 
          @click="$router.push('/')"
          aria-label="返回首页">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
        </button>
        
        <!-- 登录状态下显示"进入游戏"按钮 -->
        <button 
          v-if="isLogin"
          class="enter-game-btn"
          @click="handleEnterGame"
        >
          进入游戏
        </button>
        
        <!-- 未登录状态下显示登录/注册按钮 -->
        <template v-else>
          <router-link to="/login" class="login-link">登录</router-link>
          <router-link to="/register" class="register-link">注册</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

// 定义组件属性
defineProps({
  // 导航栏标题，默认"五子棋对战"
  title: {
    type: String,
    default: '五子棋对战'
  },
  // 是否显示返回按钮
  showBackButton: {
    type: Boolean,
    default: true
  },
  // 是否显示"已有账号"提示
  showLoginHint: {
    type: Boolean,
    default: false
  },
  // 是否强制显示登录/注册按钮（即使已登录）
  forceShowAuthBtns: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const isLogin = ref(false)

// 检查用户登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLogin.value = !!token
}

// 处理"进入游戏"按钮点击事件
const handleEnterGame = () => {
  router.push('/home')
}

onMounted(() => {
  // 组件挂载时检查登录状态
  checkLoginStatus()
})
</script>

<style scoped>
.navbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.25);
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-shadow: var(--shadow-lg);
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
}

.nav-logo h1:hover {
  transform: scale(1.02);
  color: var(--primary-color);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  background-color: var(--bg-primary);
  border: 1px solid var(--border-light);
  color: var(--text-primary);
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  gap: 6px;
}

.back-btn:hover {
  background-color: var(--bg-secondary);
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateX(-2px);
}

.enter-game-btn {
  background-color: var(--primary-color);
  border: none;
  color: white;
  padding: 12px 28px;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 16px;
  font-weight: 700;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.enter-game-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
  background-color: var(--primary-hover);
}

.enter-game-btn:active {
  transform: translateY(0);
}

.login-link, .register-link {
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 90px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.login-link {
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
  background-color: var(--bg-primary);
}

.login-link:hover {
  background-color: var(--primary-color);
  color: white;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.register-link {
  background-color: var(--primary-color);
  color: white;
  border: 1px solid var(--primary-color);
}

.register-link:hover {
  background-color: var(--primary-hover);
  border-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.login-hint {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  transition: all var(--transition-fast);
}

.login-hint:hover {
  color: var(--primary-color);
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-container {
    padding: 0 10px;
  }
  
  .nav-logo h1 {
    font-size: 24px;
  }
  
  .nav-actions {
    gap: 10px;
  }
  
  .enter-game-btn,
  .login-link,
  .register-link,
  .back-btn {
    padding: 6px 12px;
    font-size: 14px;
    min-width: 70px;
  }
}
</style>
