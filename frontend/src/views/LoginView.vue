<template>
  <div class="login-container">
    <div class="login-form">
      <div class="form-header">
        <h2>用户登录</h2>
        <p class="form-subtitle">欢迎回到五子棋游戏</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-width="80px"
        size="large"
        class="login-form-content"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名"
            clearable
            autofocus
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="记住我" prop="remember">
          <el-checkbox v-model="loginForm.remember">
            记住登录状态
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleLogin" 
            :loading="loading"
            size="large"
            style="width: 100%"
          >
            立即登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="form-footer">
        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
        <div class="other-links">
          <router-link to="/forgot-password">忘记密码？</router-link>
        </div>
      </div>
    </div>

    <!-- 登录失败对话框 -->
    <el-dialog
      v-model="errorDialog.visible"
      title="登录失败"
      width="400px"
    >
      <div class="error-content">
        <div class="error-icon">
          <el-icon color="#F56C6C" :size="64">
            <CircleClose />
          </el-icon>
        </div>
        <h3>{{ errorDialog.title }}</h3>
        <p>{{ errorDialog.message }}</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="errorDialog.visible = false" type="primary">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, CircleClose } from '@element-plus/icons-vue'
import { userAPI } from '../api/index.js'

export default {
  name: 'LoginView',
  components: {
    User,
    Lock,
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
      handleLogin
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.3) 0%, transparent 50%);
  animation: float 20s ease-in-out infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

.login-form {
  background: rgba(255, 255, 255, 0.95);
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 420px;
  text-align: center;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.login-form:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.15);
}

.form-header {
  margin-bottom: 30px;
}

.form-header h1 {
  color: #333;
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.form-subtitle {
  color: #666;
  font-size: 14px;
  margin-top: 8px;
  font-weight: 400;
}

.form-item {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.login-button.is-loading {
  background: linear-gradient(45deg, #a0aec0, #718096);
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #764ba2;
  text-decoration: underline;
}

.register-link {
  margin-top: 25px;
  color: #666;
  font-size: 14px;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.register-link a:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .login-form {
    background: rgba(30, 30, 40, 0.95);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .form-header h1 {
    background: linear-gradient(45deg, #a0aec0, #cbd5e0);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .form-subtitle,
  .remember-forgot,
  .register-link {
    color: #cbd5e0;
  }
  
  .forgot-password,
  .register-link a {
    color: #90cdf4;
  }
  
  .forgot-password:hover,
  .register-link a:hover {
    color: #63b3ed;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 15px;
  }
  
  .login-form {
    padding: 30px 20px;
    border-radius: 15px;
  }
  
  .form-header h1 {
    font-size: 24px;
  }
  
  .login-button {
    height: 44px;
    font-size: 15px;
  }
  
  .remember-forgot {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .login-form {
    padding: 25px 15px;
  }
  
  .form-header h1 {
    font-size: 22px;
  }
  
  .form-subtitle {
    font-size: 13px;
  }
  
  .login-button {
    height: 42px;
    font-size: 14px;
  }
}

/* 错误对话框样式 */
.error-dialog {
  border-radius: 15px;
}

.error-dialog .el-dialog__header {
  padding: 20px 20px 10px;
  border-bottom: 1px solid #f0f0f0;
}

.error-dialog .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: #e53e3e;
}

.error-dialog .el-dialog__body {
  padding: 20px;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  line-height: 1.5;
  color: #4a5568;
}

.error-icon {
  font-size: 24px;
  color: #e53e3e;
  flex-shrink: 0;
}
</style>
