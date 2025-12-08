<template>
  <div class="login-container">
    <!-- 通用导航栏 -->
    <GameNavbar 
      :show-back-btn="true"
    />

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧五子棋插画 -->
      <div class="character-section">
        <div class="character-image">
          <div class="gobang-board">
            <div class="board-grid">
              <div 
                v-for="i in 9" 
                :key="i" 
                class="board-row"
              >
                <div 
                  v-for="j in 9" 
                  :key="j" 
                  class="board-cell"
                  :class="{
                    'black-piece': (i === 5 && j === 5) || (i === 7 && j === 3) || (i === 3 && j === 7),
                    'white-piece': (i === 4 && j === 5) || (i === 5 && j === 4) || (i === 5 && j === 6)
                  }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间登录表单 -->
      <div class="login-section">
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          size="large"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <div class="form-group">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名"
              clearable
              autofocus
            ></el-input>
          </div>

          <div class="form-group">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
            ></el-input>
          </div>

          <div class="form-options">
            <el-checkbox v-model="loginForm.agreement">
              我已详细阅读并同意《用户协议与隐私政策》
            </el-checkbox>
          </div>

          <button 
            class="login-btn" 
            @click="handleLogin" 
            :disabled="loading || !loginForm.agreement"
          >
            <span v-if="!loading">开始游戏</span>
            <span v-else>登录中...</span>
          </button>

          <div class="register-link">
            <router-link to="/register">立即注册</router-link>
          </div>
        </el-form>

        <!-- 其他登录方式 -->
        <div class="other-login">
          <div class="login-divider">
            <span>其他登录方式</span>
          </div>
          <div class="login-icons">
            <div class="login-icon" title="微信登录">
              <el-icon><WeChat /></el-icon>
            </div>
            <div class="login-icon" title="QQ登录">
              <el-icon><ChatDotRound /></el-icon>
            </div>
          </div>
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
import {
  WeChat,
  CircleClose,
  ChatDotRound
} from '@element-plus/icons-vue'
import { userAPI } from '../api/index.js'
import GameNavbar from '../components/GameNavbar.vue'

export default {
  name: 'LoginView',
  components: {
    WeChat,
    CircleClose,
    ChatDotRound,
    GameNavbar
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const loginFormRef = ref(null)
    const loading = ref(false)

    const loginForm = reactive({
      username: '',
      password: '',
      agreement: false
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
  background-color: var(--bg-primary);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  display: flex;
  flex-direction: column;
  padding: 0;
  margin: 0;
}

/* 主要内容区域 */
.main-content {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  max-width: 1000px;
  gap: 60px;
  margin: 40px auto;
  padding: 0 20px;
  flex: 1;
}

/* 左侧五子棋插画 */
.character-section {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
  min-width: 250px;
}

.character-image {
  position: relative;
  width: 250px;
  height: 250px;
  animation: fadeIn var(--transition-normal);
}

.gobang-board {
  display: flex;
  justify-content: center;
  align-items: center;
}

.board-grid {
  display: grid;
  grid-template-rows: repeat(9, 25px);
  grid-template-columns: repeat(9, 25px);
  background: var(--board-color);
  padding: 15px;
  border-radius: 8px;
  box-shadow: var(--shadow-md);
  border: 2px solid var(--board-border);
}

.board-row {
  display: contents;
}

.board-cell {
  width: 25px;
  height: 25px;
  position: relative;
  border: 1px solid var(--board-line);
}

.board-cell::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 20px;
  border-radius: 50%;
  z-index: 1;
  transition: all var(--transition-fast);
}

.black-piece::before {
  background: var(--black-stone);
  box-shadow: var(--shadow-sm);
}

.white-piece::before {
  background: var(--white-stone);
  box-shadow: var(--shadow-sm);
}

/* 中间登录表单 */
.login-section {
  width: 320px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  padding: 32px;
  border-radius: var(--radius-md);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  animation: fadeIn var(--transition-normal);
  transition: all var(--transition-normal);
}

.login-section:hover {
  box-shadow: var(--shadow-md);
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 20px;
}

.form-group .el-input {
  width: 100%;
}

.form-options {
  margin-bottom: 25px;
  font-size: 12px;
  text-align: left;
}

.form-options .el-checkbox__label {
  color: var(--text-secondary);
  font-weight: 400;
}

/* 登录按钮样式 */
.login-btn {
  width: 100%;
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: var(--radius-sm);
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
  font-family: inherit;
}

.login-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  background: var(--bg-tertiary);
  color: var(--text-tertiary);
  cursor: not-allowed;
  box-shadow: none;
}

/* 注册链接 */
.register-link {
  text-align: right;
  font-size: 14px;
  margin-bottom: 24px;
}

.register-link a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--transition-fast);
}

.register-link a:hover {
  color: var(--primary-hover);
  text-decoration: none;
}

/* 其他登录方式 */
.other-login {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
}

.login-divider {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.login-divider::before,
.login-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border-light);
}

.login-divider span {
  padding: 0 12px;
  color: var(--text-tertiary);
  font-size: 14px;
  font-weight: 400;
}

.login-icons {
  display: flex;
  justify-content: center;
  gap: 40px;
}

.login-icon {
  font-size: 28px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.login-icon:hover {
  color: var(--primary-color);
  transform: scale(1.05);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
    gap: 40px;
    margin: 20px auto;
  }
  
  .character-section {
    margin-bottom: 20px;
  }
  
  .login-section {
    width: 100%;
    max-width: 400px;
  }
}

@media (max-width: 480px) {
  .character-image {
    width: 200px;
    height: 200px;
  }
  
  .board-grid {
    grid-template-rows: repeat(9, 20px);
    grid-template-columns: repeat(9, 20px);
  }
  
  .board-cell {
    width: 20px;
    height: 20px;
  }
  
  .board-cell::before {
    width: 16px;
    height: 16px;
  }
  
  .login-section {
    padding: 24px;
    border-radius: 12px;
  }
}

/* 错误对话框样式 */
.error-content {
  text-align: center;
  padding: 24px 0;
}

.error-icon {
  margin-bottom: 16px;
  animation: pulse 1.5s infinite;
}

.error-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.error-content p {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}
</style>
