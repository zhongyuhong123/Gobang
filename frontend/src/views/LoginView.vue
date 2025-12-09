<template>
  <div class="login-container">
    <!-- 液态玻璃背景效果 -->
    <div class="glass-bg">
      <div class="glass-blob glass-blob-1"></div>
      <div class="glass-blob glass-blob-2"></div>
      <div class="glass-blob glass-blob-3"></div>
      <div class="glass-blob glass-blob-4"></div>
    </div>
    
    <!-- 粒子动画背景 -->
    <div class="particles-bg">
      <div v-for="n in 15" :key="n" class="particle" :style="getParticleStyle(n)"></div>
    </div>

    <!-- 主登录卡片 -->
    <div class="login-wrapper">
      <div class="login-card">
        <!-- 左侧品牌区域 -->
        <div class="brand-panel">
          <div class="brand-content">
            <div class="logo-section">
              <h1 class="brand-title">五子棋</h1>
            </div>
            
            <!-- 简约棋盘展示 -->
            <div class="board-showcase">
              <div class="simple-board">
                <div class="simple-piece black"></div>
                <div class="simple-piece white"></div>
                <div class="simple-piece black"></div>
              </div>
            </div>
            

          </div>
        </div>
        
        <!-- 右侧表单区域 -->
        <div class="form-panel">
          <div class="form-content">
            <div class="form-header">
              <h2 class="form-title">欢迎回来</h2>
              <p class="form-subtitle">登录您的账户</p>
            </div>
            
            <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="rules"
              class="login-form"
              @keyup.enter="handleLogin"
              size="large"
            >
              <el-form-item prop="username">
                 <el-input 
                   v-model="loginForm.username" 
                   placeholder="用户名"
                   clearable
                   autofocus
                   class="form-input"
                 />
               </el-form-item>

               <el-form-item prop="password">
                 <el-input
                   v-model="loginForm.password"
                   type="password"
                   placeholder="密码"
                   show-password
                   clearable
                   class="form-input"
                 />
               </el-form-item>



              <el-button 
                 type="primary"
                 class="login-button"
                 @click="handleLogin" 
                 :loading="loading"
                 :disabled="!loginForm.username || !loginForm.password"
               >
                 登录
               </el-button>
            </el-form>
            
            <div class="form-links">
               <a href="/forgot-password" class="link-text">忘记密码？</a>
               <span class="link-separator">|</span>
               <a href="/register" class="link-text">立即注册</a>
             </div>
            

          </div>
        </div>
      </div>
    </div>
    
    <!-- 错误提示对话框 -->
    <el-dialog
      v-model="errorDialog.visible"
      title="登录失败"
      width="380px"
      class="error-dialog"
      :close-on-click-modal="false"
    >
      <div class="error-content">
        <div class="error-icon">
          <el-icon color="#F56C6C" :size="48">
            <CircleClose />
          </el-icon>
        </div>
        <h3 class="error-title">{{ errorDialog.title }}</h3>
        <p class="error-message">{{ errorDialog.message }}</p>
      </div>
      <template #footer>
        <el-button @click="errorDialog.visible = false" type="primary" class="error-btn">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CircleClose
} from '@element-plus/icons-vue'
import { userAPI } from '../api/index.js'

export default {
  name: 'LoginView',
  components: {
    CircleClose
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const loginFormRef = ref(null)
    const loading = ref(false)

    const loginForm = reactive({
      username: '',
      password: '',
      remember: false
    })

    // 错误对话框
    const errorDialog = reactive({
      visible: false,
      title: '登录失败',
      message: ''
    })

    // 登录失败计数
    const loginAttempts = reactive({
      count: 0,
      lastAttempt: null,
      lockoutTime: 0
    })

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
      ]
    }

    // 检查是否被锁定
    const checkLockout = () => {
      const now = Date.now()
      if (loginAttempts.lockoutTime > now) {
        const remainingTime = Math.ceil((loginAttempts.lockoutTime - now) / 1000)
        errorDialog.title = '账户暂时锁定'
        errorDialog.message = `由于多次登录失败，您的账户已被锁定。请等待 ${remainingTime} 秒后重试。`
        errorDialog.visible = true
        return true
      }
      return false
    }

    // 更新登录尝试计数
    const updateLoginAttempts = (success) => {
      const now = Date.now()
      
      if (success) {
        // 重置计数
        loginAttempts.count = 0
        loginAttempts.lastAttempt = null
        loginAttempts.lockoutTime = 0
      } else {
        loginAttempts.count++
        loginAttempts.lastAttempt = now
        
        // 连续失败5次，锁定5分钟
        if (loginAttempts.count >= 5) {
          loginAttempts.lockoutTime = now + 5 * 60 * 1000 // 5分钟
        }
      }
      
      // 保存到本地存储
      localStorage.setItem('loginAttempts', JSON.stringify(loginAttempts))
    }

    // 加载登录尝试记录
    const loadLoginAttempts = () => {
      const saved = localStorage.getItem('loginAttempts')
      if (saved) {
        const data = JSON.parse(saved)
        Object.assign(loginAttempts, data)
      }
    }

    // 自动填充记住的用户名
    const autoFillUsername = () => {
      const rememberedUsername = localStorage.getItem('rememberedUsername')
      if (rememberedUsername) {
        loginForm.username = rememberedUsername
        loginForm.remember = true
      }
    }

    const handleLogin = async () => {
      if (!loginFormRef.value) return

      // 检查锁定状态
      if (checkLockout()) {
        return
      }

      await loginFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            const response = await userAPI.login({
              username: loginForm.username,
              password: loginForm.password
            })
            
            if (response.success || response.code === 200) {
              // 登录成功
              updateLoginAttempts(true)
              
              // 处理记住我功能
              if (loginForm.remember) {
                localStorage.setItem('rememberedUsername', loginForm.username)
              } else {
                localStorage.removeItem('rememberedUsername')
              }
              
              // 保存token和用户信息
              const token = response.data?.token || response.token
              const userInfo = response.data?.user || response.user
              
              if (token) {
                localStorage.setItem('token', token)
                localStorage.setItem('userInfo', JSON.stringify(userInfo))
              }
              
              ElMessage.success('登录成功！')
              
              // 跳转到首页或原始目标页面
              const redirect = route.query.redirect || '/'
              router.push(redirect)
              
            } else {
              // 登录失败
              updateLoginAttempts(false)
              
              errorDialog.title = '登录失败'
              errorDialog.message = response.message || '用户名或密码错误'
              errorDialog.visible = true
              
              // 显示剩余尝试次数
              if (loginAttempts.count > 0 && loginAttempts.count < 5) {
                const remaining = 5 - loginAttempts.count
                ElMessage.warning(`登录失败，还有 ${remaining} 次尝试机会`)
              }
            }
          } catch (error) {
            updateLoginAttempts(false)
            
            console.error('登录错误:', error)
            errorDialog.title = '登录失败'
            errorDialog.message = '网络连接失败，请检查网络连接后重试'
            errorDialog.visible = true
          } finally {
            loading.value = false
          }
        }
      })
    }

    // 忘记密码
    const handleForgotPassword = () => {
      ElMessage.info('密码重置功能开发中，请联系客服')
    }

    // 社交登录
    const handleSocialLogin = (type) => {
      ElMessage.info(`${type === 'wechat' ? '微信' : 'QQ'}登录功能开发中`)
    }

    // 获取棋盘棋子样式
    const getBoardPiece = (i, j) => {
      const pieces = {
        'black-piece': [[3, 3], [5, 5], [7, 7], [2, 6], [6, 2]],
        'white-piece': [[4, 3], [3, 4], [5, 4], [4, 5], [6, 7]]
      }
      
      for (const [className, positions] of Object.entries(pieces)) {
        if (positions.some(([pi, pj]) => pi === i && pj === j)) {
          return className
        }
      }
      return ''
    }

    // 获取粒子样式
    const getParticleStyle = () => {
      const size = Math.random() * 4 + 2
      const left = Math.random() * 100
      const top = Math.random() * 100
      const delay = Math.random() * 8
      const duration = Math.random() * 15 + 10
      
      return {
        width: `${size}px`,
        height: `${size}px`,
        left: `${left}%`,
        top: `${top}%`,
        animationDelay: `${delay}s`,
        animationDuration: `${duration}s`
      }
    }

    onMounted(() => {
      loadLoginAttempts()
      autoFillUsername()
    })

    return {
      loginForm,
      rules,
      loginFormRef,
      loading,
      errorDialog,
      handleLogin,
      handleForgotPassword,
      handleSocialLogin,
      getBoardPiece,
      getParticleStyle
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 25%, #cbd5e1 50%, #94a3b8 100%);
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  padding: 10px;
}

/* 液态玻璃背景效果 - iOS 26级别 */
.glass-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  filter: blur(0.5px);
}

.glass-blob {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.4), rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0.02));
  backdrop-filter: blur(60px) saturate(180%);
  -webkit-backdrop-filter: blur(60px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 
    0 0 80px rgba(255, 255, 255, 0.2),
    inset 0 0 40px rgba(255, 255, 255, 0.1),
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  animation: liquid-float 20s infinite cubic-bezier(0.45, 0, 0.55, 1);
  transform-origin: center;
}

.glass-blob-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  left: -150px;
  animation-delay: 0s;
}

.glass-blob-2 {
  width: 200px;
  height: 200px;
  top: 50%;
  right: -100px;
  animation-delay: -5s;
}

.glass-blob-3 {
  width: 150px;
  height: 150px;
  bottom: -75px;
  left: 30%;
  animation-delay: -10s;
}

.glass-blob-4 {
  width: 100px;
  height: 100px;
  top: 20%;
  left: 60%;
  animation-delay: -15s;
}

@keyframes liquid-float {
  0%, 100% { 
    transform: translate(0, 0) scale(1) rotate(0deg);
    border-radius: 50%;
    filter: blur(0px);
  }
  20% { 
    transform: translate(40px, -20px) scale(1.15) rotate(72deg);
    border-radius: 60% 40% 50% 70%;
    filter: blur(1px);
  }
  40% { 
    transform: translate(-30px, 30px) scale(0.85) rotate(144deg);
    border-radius: 40% 70% 60% 50%;
    filter: blur(0.5px);
  }
  60% { 
    transform: translate(20px, -40px) scale(1.25) rotate(216deg);
    border-radius: 70% 50% 40% 60%;
    filter: blur(1.5px);
  }
  80% { 
    transform: translate(-40px, 20px) scale(0.95) rotate(288deg);
    border-radius: 50% 60% 70% 40%;
    filter: blur(0.8px);
  }
}

/* 粒子背景 - 清新蓝白调 */
.particles-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
}

.particle {
  position: absolute;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.4), rgba(147, 197, 253, 0.15), rgba(219, 234, 254, 0.05));
  border-radius: 50%;
  animation: particle-float 25s infinite linear;
  pointer-events: none;
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.2);
}

@keyframes particle-float {
  0% {
    transform: translateY(100vh) translateX(0) scale(0.5);
    opacity: 0;
  }
  10% {
    opacity: 1;
    transform: translateY(90vh) translateX(10px) scale(1);
  }
  90% {
    opacity: 1;
    transform: translateY(10vh) translateX(40px) scale(1);
  }
  100% {
    transform: translateY(-100vh) translateX(50px) scale(0.5);
    opacity: 0;
  }
}

/* 主登录卡片 - 清新液态玻璃 */
.login-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 900px;
  padding: 20px;
}

.login-card {
  display: flex;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(60px) saturate(180%);
  -webkit-backdrop-filter: blur(60px) saturate(180%);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 
    0 32px 64px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.6) inset,
    0 0 80px rgba(59, 130, 246, 0.15);
  overflow: hidden;
  width: 100%;
  max-width: 900px;
  min-height: 500px;
  animation: card-appear 0.8s cubic-bezier(0.23, 1, 0.32, 1);
  position: relative;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(30, 64, 175, 0.04) 0%, 
    transparent 50%, 
    rgba(30, 41, 59, 0.02) 100%);
  pointer-events: none;
  z-index: 1;
}

@keyframes card-appear {
  0% {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%) translateY(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) translateY(100%) rotate(45deg);
  }
}

/* 品牌面板 - 清新蓝白调 */
.brand-panel {
  flex: 1;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 50%, #93c5fd 100%);
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  position: relative;
  overflow: hidden;
  border-right: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.brand-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse"><path d="M 10 0 L 0 0 0 10" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="0.5"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
  opacity: 0.3;
}

.brand-content {
  position: relative;
  z-index: 1;
}

.logo-section {
  margin-bottom: 40px;
}

.logo-circle {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.logo-text {
  font-size: 36px;
  font-weight: bold;
  color: white;
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  color: white;
  margin-bottom: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.5px;
}

.brand-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

/* 棋盘展示 */
.chess-showcase {
  margin: 30px 0;
}

.mini-board {
  background: #8b7355;
  padding: 15px;
  border-radius: 12px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  display: inline-block;
}

.board-row {
  display: flex;
}

.board-cell {
  width: 20px;
  height: 20px;
  background: #d4c5a0;
  border: 1px solid #8b7355;
  position: relative;
  cursor: pointer;
  transition: all 0.2s ease;
}

.board-cell:hover {
  background: #e5d6b3;
}

.board-cell.black-piece::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 16px;
  background: radial-gradient(circle at 30% 30%, #333, #000);
  border-radius: 50%;
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.board-cell.white-piece::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 16px;
  background: radial-gradient(circle at 30% 30%, #fff, #ddd);
  border-radius: 50%;
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}



/* 简约棋盘展示 - 清新风格 */
.board-showcase {
  margin: 20px 0;
}

.simple-board {
  background: rgba(255, 255, 255, 0.2);
  padding: 12px;
  border-radius: 20px;
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 0 0 1px rgba(255, 255, 255, 0.4),
    inset 0 0 20px rgba(59, 130, 246, 0.1);
  display: inline-flex;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.simple-board::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent 30%, rgba(30, 64, 175, 0.06) 50%, transparent 70%);
  animation: shimmer 3s infinite;
}

.simple-piece {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.3),
    inset 0 2px 4px rgba(255, 255, 255, 0.4),
    inset 0 -2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
  position: relative;
  overflow: hidden;
}

.simple-piece:hover {
  transform: scale(1.15) translateY(-2px);
  box-shadow: 
    0 6px 20px rgba(0, 0, 0, 0.4),
    inset 0 2px 6px rgba(255, 255, 255, 0.5),
    inset 0 -2px 6px rgba(0, 0, 0, 0.15);
}

.simple-piece.black {
  background: radial-gradient(circle at 35% 35%, #1e293b, #0f172a, #020617);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.3),
    inset 0 2px 4px rgba(255, 255, 255, 0.3),
    inset 0 -2px 4px rgba(0, 0, 0, 0.2);
}

.simple-piece.white {
  background: radial-gradient(circle at 35% 35%, #f8fafc, #e2e8f0, #cbd5e1);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    inset 0 2px 4px rgba(255, 255, 255, 0.8),
    inset 0 -2px 4px rgba(0, 0, 0, 0.1);
}

.simple-piece::after {
  content: '';
  position: absolute;
  top: 15%;
  left: 15%;
  width: 30%;
  height: 30%;
  background: radial-gradient(circle, rgba(148, 163, 184, 0.6), transparent);
  border-radius: 50%;
  filter: blur(1px);
}

/* 表单面板 - 清新风格 */
.form-panel {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(50px) saturate(150%);
  -webkit-backdrop-filter: blur(50px) saturate(150%);
  position: relative;
}

.form-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, 
    rgba(30, 64, 175, 0.05) 0%, 
    transparent 30%, 
    transparent 70%, 
    rgba(15, 23, 42, 0.03) 100%);
  pointer-events: none;
  z-index: 1;
}

.form-content {
  max-width: 320px;
  margin: 0 auto;
  width: 100%;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.form-subtitle {
  color: #64748b;
  font-size: 16px;
  margin-bottom: 32px;
  font-weight: 400;
}

/* 表单样式 - 清新风格 */
.login-form {
  margin-bottom: 16px;
}

.form-input {
  --el-input-border-radius: 12px;
  --el-input-bg-color: rgba(255, 255, 255, 0.25);
  --el-input-border-color: rgba(255, 255, 255, 0.4);
  --el-input-hover-border-color: rgba(59, 130, 246, 0.6);
  --el-input-focus-border-color: rgba(59, 130, 246, 0.9);
}

.form-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: none;
  transition: all 0.3s ease;
}

.form-input :deep(.el-input__wrapper):hover {
  border-color: rgba(59, 130, 246, 0.6);
  background: rgba(255, 255, 255, 0.35);
}

.form-input :deep(.el-input__wrapper).is-focus {
  border-color: rgba(59, 130, 246, 0.9);
  background: rgba(255, 255, 255, 0.4);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.login-button {
  width: 100%;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(30, 64, 175, 0.9), rgba(59, 130, 246, 0.9));
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 
    0 4px 20px rgba(30, 64, 175, 0.3),
    inset 0 1px 2px rgba(255, 255, 255, 0.2);
}

.login-button:hover {
  background: linear-gradient(135deg, rgba(30, 64, 175, 1), rgba(59, 130, 246, 1));
  box-shadow: 
    0 6px 24px rgba(30, 64, 175, 0.4),
    inset 0 1px 2px rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.login-button:active {
  transform: translateY(0);
}

/* 表单链接 */
.form-links {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 16px;
  font-size: 13px;
}

.link-text {
  color: rgba(59, 130, 246, 0.9);
  text-decoration: none;
  padding: 4px 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.link-text:hover {
  color: rgba(30, 64, 175, 1);
  text-shadow: 0 0 8px rgba(59, 130, 246, 0.3);
}

.link-separator {
  color: rgba(255, 255, 255, 0.4);
  margin: 0 4px;
  font-weight: 300;
}

/* 社交登录 */
.social-login {
  display: none;
}

/* 错误对话框 */
.error-dialog {
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
}

.error-content {
  text-align: center;
  padding: 20px 0;
}

.error-icon {
  margin-bottom: 16px;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.error-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.error-message {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

.error-btn {
  width: 100%;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.9), rgba(124, 58, 237, 0.9)) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    max-width: 400px;
  }
  
  .brand-panel {
    padding: 40px 30px;
  }
  
  .form-panel {
    padding: 40px 30px;
  }
  
  .brand-title {
    font-size: 24px;
  }
  
  .form-title {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .login-wrapper {
    padding: 10px;
  }
  
  .login-card {
    border-radius: 20px;
  }
  
  .brand-panel {
    padding: 30px 20px;
  }
  
  .form-panel {
    padding: 30px 20px;
  }
  
  .logo-circle {
    width: 60px;
    height: 60px;
  }
  
  .logo-text {
    font-size: 28px;
  }
  
  .mini-board {
    padding: 10px;
  }
  
  .board-cell {
    width: 16px;
    height: 16px;
  }
  
  .board-cell.black-piece::after,
  .board-cell.white-piece::after {
    width: 12px;
    height: 12px;
  }
}
</style>