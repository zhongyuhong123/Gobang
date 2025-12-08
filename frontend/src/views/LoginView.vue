<template>
  <div class="login-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-container">
        <div class="nav-logo">
          <h1>五子棋</h1>
        </div>
        <div class="nav-actions">
          <button class="back-btn" @click="$router.push('/')">
            返回首页
          </button>
        </div>
      </div>
    </nav>

    <div class="login-content">
      <div class="login-card">
        <div class="card-header">
          <h2>用户登录</h2>
          <p>欢迎回到五子棋游戏</p>
        </div>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          size="large"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <div class="form-group">
            <label for="username">用户名</label>
            <el-input 
              id="username"
              v-model="loginForm.username" 
              placeholder="请输入用户名"
              clearable
              autofocus
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <el-input
              id="password"
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
          </div>

          <div class="form-options">
            <el-checkbox v-model="loginForm.remember">
              记住登录状态
            </el-checkbox>
            <router-link to="/forgot-password" class="forgot-link">
              忘记密码？
            </router-link>
          </div>

          <button 
            class="login-btn" 
            @click="handleLogin" 
            :disabled="loading"
          >
            <span v-if="!loading">立即登录</span>
            <span v-else>登录中...</span>
          </button>
        </el-form>

        <div class="card-footer">
          <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
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
  background: #f8f9fa;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 导航栏样式 */
.navbar {
  background: white;
  border-bottom: 1px solid #e9ecef;
  position: fixed;
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
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.back-btn {
  background: transparent;
  color: #667eea;
  border: 1px solid #667eea;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #667eea;
  color: white;
}

/* 登录内容样式 */
.login-content {
  padding-top: 70px;
  min-height: calc(100vh - 70px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.card-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.card-header p {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

/* 表单样式 */
.login-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 8px;
}

/* 表单选项样式 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  font-size: 14px;
}

.forgot-link {
  color: #667eea;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 登录按钮样式 */
.login-btn {
  width: 100%;
  background: #667eea;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s ease;
}

.login-btn:hover:not(:disabled) {
  background: #5a6fd8;
}

.login-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

/* 底部链接样式 */
.card-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.card-footer p {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

.card-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.card-footer a:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    padding: 30px 20px;
    margin: 0 10px;
  }
  
  .card-header h2 {
    font-size: 24px;
  }
  
  .form-options {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
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
