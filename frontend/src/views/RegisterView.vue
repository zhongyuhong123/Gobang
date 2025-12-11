<template>
  <div class="register-container">
    <div class="glass-bg">
      <div class="glass-blob glass-blob-1"></div>
      <div class="glass-blob glass-blob-2"></div>
      <div class="glass-blob glass-blob-3"></div>
      <div class="glass-blob glass-blob-4"></div>
    </div>
    <div class="particles-bg">
      <div v-for="n in 8" :key="n" class="particle" :style="getParticleStyle(n)"></div>
    </div>
    <div class="register-wrapper">
      <div class="register-card">
        <div class="brand-panel">
          <div class="brand-content">
            <div class="logo-section">
              <h1 class="brand-title">五子棋</h1>
            </div>
            <div class="board-showcase">
              <div class="simple-board">
                <div class="simple-piece black"></div>
                <div class="simple-piece white"></div>
                <div class="simple-piece black"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="form-panel">
          <div class="form-content">
            <div class="form-header">
              <h2 class="form-title">创建账户</h2>
              <p class="form-subtitle">开始您的五子棋之旅</p>
            </div>
            <el-form ref="registerFormRef" :model="registerForm" :rules="rules" class="register-form" @keyup.enter="handleRegister" size="large">
              <el-form-item prop="username">
                <el-input v-model="registerForm.username" placeholder="用户名" clearable autofocus class="form-input" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="密码" show-password clearable class="form-input" />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" show-password clearable class="form-input" />
              </el-form-item>
              <el-button type="primary" class="register-button" @click="handleRegister" :loading="loading">
                注册
              </el-button>
            </el-form>
            <div class="form-links">
              <span class="link-text">已有账户？</span>
              <a href="/login" class="link-text">立即登录</a>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { userAPI } from '../api/index.js'

export default {
  name: 'RegisterView',
  components: {
  },
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const loading = ref(false)

    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: ''
    })



    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }

    const handleRegister = async () => {
      if (!registerFormRef.value) return

      await registerFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            const response = await userAPI.register({
              username: registerForm.username,
              password: registerForm.password
            })

            if (response.success || response.code === 200 || response.status === true) {
              ElMessage.success('注册成功！')
              
              setTimeout(() => {
                router.push('/login')
              }, 1500)
            } else {
              ElMessage.error(response.message || response.reason || '注册失败，请重试')
            }
          } catch (error) {
            console.error('注册错误:', error)
            ElMessage.error('网络连接失败，请检查网络连接后重试')
          } finally {
            loading.value = false
          }
        }
      })
    }

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

    return {
      registerForm,
      rules,
      registerFormRef,
      loading,
      handleRegister,
      getParticleStyle
    }
  }
}
</script>

<style scoped>
.register-container {
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
  background: radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.5), rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.05));
  backdrop-filter: blur(80px) saturate(200%);
  -webkit-backdrop-filter: blur(80px) saturate(200%);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 
    0 0 100px rgba(255, 255, 255, 0.3),
    inset 0 0 50px rgba(255, 255, 255, 0.15),
    0 12px 40px rgba(0, 0, 0, 0.08),
    inset 0 2px 0 rgba(255, 255, 255, 0.7),
    0 0 0 1px rgba(255, 255, 255, 0.2);
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
  pointer-events: none;
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.2);
}



.register-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 900px;
  padding: 20px;
}

.register-card {
  display: flex;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(100px) saturate(220%);
  -webkit-backdrop-filter: blur(100px) saturate(220%);
  border-radius: 28px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 
    0 40px 80px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.8) inset,
    0 0 120px rgba(59, 130, 246, 0.2),
    inset 0 0 60px rgba(255, 255, 255, 0.1);
  overflow: hidden;
  width: 100%;
  max-width: 900px;
  min-height: 500px;
  position: relative;
}

.register-card::before {
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
  border-right: 1px solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(40px) saturate(150%);
  -webkit-backdrop-filter: blur(40px) saturate(150%);
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

.brand-title {
  font-size: 36px;
  font-weight: 700;
  color: white;
  margin-bottom: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.5px;
}

.board-showcase {
  margin: 20px 0;
}

.simple-board {
  background: rgba(255, 255, 255, 0.25);
  padding: 16px;
  border-radius: 24px;
  backdrop-filter: blur(30px) saturate(180%);
  -webkit-backdrop-filter: blur(30px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 
    0 12px 40px rgba(0, 0, 0, 0.08),
    inset 0 0 0 1px rgba(255, 255, 255, 0.6),
    inset 0 0 30px rgba(59, 130, 246, 0.15);
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
}



.simple-piece {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.3),
    inset 0 2px 4px rgba(255, 255, 255, 0.4),
    inset 0 -2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
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

.form-panel {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(70px) saturate(180%);
  -webkit-backdrop-filter: blur(70px) saturate(180%);
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

.register-form {
  margin-bottom: 24px;
}

.form-input {
  --el-border-radius-base: 12px;
  --el-input-bg-color: rgba(255, 255, 255, 0.25);
  --el-input-border-color: rgba(255, 255, 255, 0.4);
  --el-input-hover-border-color: rgba(59, 130, 246, 0.6);
  --el-input-focus-border-color: rgba(59, 130, 246, 0.9);
}

.form-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.form-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(59, 130, 246, 0.9);
  background: rgba(255, 255, 255, 0.4);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  position: relative;
  overflow: hidden;
}

.form-links {
  text-align: center;
  margin-top: 24px;
}

.link-text {
  color: #60a5fa;
  text-decoration: none;
  font-weight: 500;
  position: relative;
}

.error-dialog :deep(.el-dialog__body) {
  padding: 32px 24px;
}

.error-content {
  text-align: center;
}

.error-icon {
  margin-bottom: 16px;
}

.error-title {
  color: #1e293b;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.error-message {
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.error-btn {
  min-width: 80px;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .register-wrapper {
    padding: 10px;
  }
  
  .register-card {
    flex-direction: column;
    min-height: auto;
  }
  
  .brand-panel {
    padding: 30px 20px;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.4);
  }
  
  .form-panel {
    padding: 30px 20px;
  }
  
  .form-content {
    max-width: 100%;
  }
  
  .form-title {
    font-size: 24px;
  }
  
  .form-subtitle {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .register-container {
    padding: 5px;
  }
  
  .brand-panel {
    padding: 20px 15px;
  }
  
  .form-panel {
    padding: 20px 15px;
  }
  
  .register-button {
    height: 44px;
    font-size: 15px;
  }
  
  .brand-title {
    font-size: 28px;
  }
}
</style>