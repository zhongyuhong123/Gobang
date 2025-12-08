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
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  display: flex;
  flex-direction: column;
}

/* 导航栏样式 */
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

/* 主要内容区域 */
.main-content {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  max-width: 1200px;
  gap: 80px;
  margin-bottom: 40px;
}

/* 左侧角色插画 */
.character-section {
  display: flex;
  justify-content: center;
  align-items: center;
}

.character-image {
  position: relative;
  width: 250px;
  height: 250px;
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
  background: #deb887;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.board-row {
  display: contents;
}

.board-cell {
  width: 25px;
  height: 25px;
  position: relative;
  border: 1px solid #a0522d;
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
}

.black-piece::before {
  background: #2c3e50;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.white-piece::before {
  background: #ecf0f1;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* 中间登录表单 */
.login-section {
  width: 300px;
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 20px;
}

.form-group .el-input {
  width: 100%;
  border-radius: 4px;
}

.form-options {
  margin-bottom: 25px;
  font-size: 12px;
  text-align: left;
}

.form-options .el-checkbox__label {
  color: #666;
}

/* 登录按钮样式 */
.login-btn {
  width: 100%;
  background: #00bfff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 15px;
}

.login-btn:hover:not(:disabled) {
  background: #0099cc;
  transform: scale(1.02);
}

.login-btn:disabled {
  background: #b3e5fc;
  cursor: not-allowed;
}

/* 注册链接 */
.register-link {
  text-align: right;
  font-size: 14px;
  margin-bottom: 30px;
}

.register-link a {
  color: #00bfff;
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}

/* 其他登录方式 */
.other-login {
  margin-top: 30px;
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
  background: #ddd;
}

.login-divider span {
  padding: 0 10px;
  color: #666;
  font-size: 14px;
}

.login-icons {
  display: flex;
  justify-content: center;
  gap: 30px;
}

.login-icon {
  font-size: 28px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.login-icon:hover {
  transform: scale(1.1);
}

/* 右侧下载区域 */


/* 响应式设计 */
@media (max-width: 992px) {
  .main-content {
    flex-direction: column;
    gap: 40px;
  }
  
  .character-section, .download-section {
    margin-bottom: 20px;
  }
}

@media (max-width: 480px) {
  .game-logo h1 {
    font-size: 36px;
  }
  
  .character-image {
    width: 150px;
    height: 150px;
  }
  
  .duck, .devil {
    font-size: 90px;
  }
  
  .login-section {
    width: 90%;
  }
  
  .age-rating {
    position: static;
    margin-top: 20px;
  }
}

/* 错误对话框样式 */
.error-content {
  text-align: center;
  padding: 20px 0;
}

.error-icon {
  margin-bottom: 20px;
}

.error-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.error-content p {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}
</style>
