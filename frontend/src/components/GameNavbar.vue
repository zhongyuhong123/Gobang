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
        
        <!-- 已有账号提示，仅在注册页显示 -->
        <router-link 
          v-if="showLoginHint"
          to="/login" 
          class="login-hint"
        >
          已有账号？去登录
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script>
export default {
  // 定义组件属性
  props: {
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
  },
  data() {
    return {
      isLogin: false
    }
  },
  methods: {
    // 检查用户登录状态
    checkLoginStatus() {
      const token = localStorage.getItem('token')
      this.isLogin = !!token
    },
    // 处理"进入游戏"按钮点击事件
    handleEnterGame() {
      this.$router.push('/home')
    }
  },
  mounted() {
    // 组件挂载时检查登录状态
    this.checkLoginStatus()
  }
}
</script>

<style scoped>
.navbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  position: sticky;
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
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s ease;
}

.nav-logo h1:hover {
  transform: scale(1.05);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  color: #606266;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #ecf5ff;
  border-color: #409EFF;
  color: #409EFF;
}

.enter-game-btn {
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  border: none;
  color: white;
  padding: 10px 24px;
  border-radius: 25px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.enter-game-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.login-link, .register-link {
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
}

.login-link {
  color: #409EFF;
  border: 1px solid #409EFF;
}

.login-link:hover {
  background: #409EFF;
  color: white;
}

.register-link {
  background: #409EFF;
  color: white;
  border: 1px solid #409EFF;
}

.register-link:hover {
  background: #66b1ff;
  border-color: #66b1ff;
}

.login-hint {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.login-hint:hover {
  color: #409EFF;
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
  }
}
</style>
