<template>
  <div class="register-container">
    <!-- 通用导航栏 -->
    <GameNavbar 
      :show-back-btn="true"
      :show-login-hint="true"
    />

    <div class="register-content">
      <div class="register-card">
        <div class="card-header">
          <h2>创建账号</h2>
          <p>创建您的五子棋游戏账户</p>
        </div>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          size="large"
          class="register-form"
        >
          <div class="form-group">
            <label for="username">用户名</label>
            <el-input 
              id="username"
              v-model="registerForm.username" 
              placeholder="请输入用户名"
              clearable
              @blur="checkUsernameAvailability"
            >
              <template #suffix>
                <div v-if="checkingUsername" class="username-check">
                  <el-icon class="is-loading"><Loading /></el-icon>
                </div>
                <div v-else-if="usernameCheck.status" class="username-check">
                  <el-icon 
                    :class="usernameCheck.available ? 'success' : 'error'"
                  >
                    <CircleCheck v-if="usernameCheck.available" />
                    <CircleClose v-else />
                  </el-icon>
                </div>
              </template>
            </el-input>
            <div v-if="usernameCheck.status" class="username-check-message" :class="usernameCheck.available ? 'success' : 'error'">
              {{ usernameCheck.message }}
            </div>
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <el-input
              id="password"
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
              @input="checkPasswordStrength"
            />
          </div>

          <div v-if="passwordStrength.text" class="password-strength">
            <label>密码强度</label>
            <el-progress 
              :percentage="passwordStrength.percentage" 
              :color="getPasswordStrengthColor()"
              :show-text="false"
              stroke-width="6"
            />
            <span class="strength-text" :class="getPasswordStrengthClass()">
              {{ passwordStrength.text }}
            </span>
          </div>

          <div class="form-group">
            <label for="confirmPassword">确认密码</label>
            <el-input
              id="confirmPassword"
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              show-password
              clearable
            />
          </div>

          <div class="form-agreement">
            <el-checkbox v-model="registerForm.agreement">
              我已阅读并同意
              <el-link type="primary" :underline="false">用户协议</el-link>
              和
              <el-link type="primary" :underline="false">隐私政策</el-link>
            </el-checkbox>
          </div>

          <button 
            class="register-btn" 
            @click="handleRegister" 
            :disabled="loading"
          >
            <span v-if="!loading">立即注册</span>
            <span v-else>注册中...</span>
          </button>
        </el-form>

        <div class="card-footer">
          <p>已有账号？<router-link to="/login">立即登录</router-link></p>
        </div>
      </div>
    </div>

    <!-- 注册成功对话框 -->
    <el-dialog
      v-model="successDialog.visible"
      title="注册成功"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="success-content">
        <div class="success-icon">
          <el-icon color="#67C23A" :size="64">
            <CircleCheck />
          </el-icon>
        </div>
        <h3>恭喜您注册成功！</h3>
        <p>您的账户已创建完成</p>
        <p v-if="successDialog.autoRedirect">
          将在 <span class="countdown">{{ successDialog.countdown }}</span> 秒后自动跳转到登录页面
        </p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="goToLogin" type="primary">
            立即登录 ({{ successDialog.countdown }}s)
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose, Loading } from '@element-plus/icons-vue'
import { userAPI } from '../api/index.js'
import GameNavbar from '../components/GameNavbar.vue'

export default {
  name: 'RegisterView',
  components: {
    CircleCheck,
    CircleClose,
    Loading,
    GameNavbar
  },
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const loading = ref(false)
    const checkingUsername = ref(false)

    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      agreement: false
    })

    // 用户名可用性检查
    const usernameCheck = reactive({
      status: false,
      available: false,
      message: ''
    })

    // 密码强度检查
    const passwordStrength = reactive({
      level: 0,
      text: '',
      percentage: 0
    })

    // 成功注册对话框
    const successDialog = reactive({
      visible: false,
      countdown: 5,
      autoRedirect: false
    })

    // 密码强度计算
    const checkPasswordStrength = () => {
      const password = registerForm.password
      let strength = 0
      
      if (password.length >= 8) strength++
      if (password.length >= 12) strength++
      if (/[a-z]/.test(password)) strength++
      if (/[A-Z]/.test(password)) strength++
      if (/[0-9]/.test(password)) strength++
      if (/[^\w\s]/.test(password)) strength++
      
      passwordStrength.level = Math.min(strength, 5)
      passwordStrength.percentage = (strength / 5) * 100
      
      const strengthLevels = [
        { text: '密码强度：弱', color: '#F56C6C' },
        { text: '密码强度：较弱', color: '#E6A23C' },
        { text: '密码强度：一般', color: '#E6A23C' },
        { text: '密码强度：较强', color: '#409EFF' },
        { text: '密码强度：强', color: '#67C23A' },
        { text: '密码强度：很强', color: '#67C23A' }
      ]
      
      if (strength > 0) {
        passwordStrength.text = strengthLevels[strength - 1].text
      } else {
        passwordStrength.level = 0
        passwordStrength.text = ''
        passwordStrength.percentage = 0
      }
    }

    // 获取密码强度样式类
    const getPasswordStrengthClass = () => {
      const classes = ['weak', 'weak', 'medium', 'medium', 'strong', 'strong']
      return classes[passwordStrength.level] || ''
    }

    // 获取密码强度颜色
    const getPasswordStrengthColor = () => {
      const colors = ['#F56C6C', '#E6A23C', '#E6A23C', '#409EFF', '#67C23A', '#67C23A']
      return colors[passwordStrength.level] || '#E4E7ED'
    }

    // 检查用户名可用性
    const checkUsernameAvailability = async () => {
      if (!registerForm.username || registerForm.username.length < 3) return
      
      checkingUsername.value = true
      try {
        // 这里可以调用后端API检查用户名是否已存在
        // 暂时模拟检查
        setTimeout(() => {
          const unavailableUsernames = ['admin', 'root', 'test', 'user']
          const isAvailable = !unavailableUsernames.includes(registerForm.username.toLowerCase())
          
          usernameCheck.status = true
          usernameCheck.available = isAvailable
          usernameCheck.message = isAvailable ? '用户名可用' : '用户名已被使用'
          checkingUsername.value = false
        }, 500)
      } catch (error) {
        console.error('检查用户名失败:', error)
        checkingUsername.value = false
      }
    }

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '用户名只能包含字母、数字、下划线和中文', trigger: 'blur' },
        { 
          validator: (rule, value, callback) => {
            if (usernameCheck.status && !usernameCheck.available) {
              callback(new Error('用户名已被使用'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' },
        { 
          validator: (rule, value, callback) => {
            if (passwordStrength.level < 2) {
              callback(new Error('密码强度不足，请使用更复杂的密码'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { 
          validator: (rule, value, callback) => {
            if (value !== registerForm.password) {
              callback(new Error('两次输入密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ],
      agreement: [
        { 
          validator: (rule, value, callback) => {
            if (!value) {
              callback(new Error('请阅读并同意用户协议和隐私政策'))
            } else {
              callback()
            }
          },
          trigger: 'change'
        }
      ]
    }

    // 开始倒计时
    const startCountdown = () => {
      successDialog.autoRedirect = true
      const timer = setInterval(() => {
        successDialog.countdown--
        if (successDialog.countdown <= 0) {
          clearInterval(timer)
          goToLogin()
        }
      }, 1000)
    }

    // 跳转到登录页面
    const goToLogin = () => {
      router.push('/login')
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
            
            if (response.success || response.code === 200) {
              // 显示成功对话框
              successDialog.visible = true
              startCountdown()
            } else {
              ElMessage.error(response.message || '注册失败')
            }
          } catch (error) {
            ElMessage.error('注册失败，请稍后重试')
            console.error('注册错误:', error)
          } finally {
            loading.value = false
          }
        }
      })
    }

    return {
      registerForm,
      rules,
      registerFormRef,
      loading,
      checkingUsername,
      usernameCheck,
      passwordStrength,
      successDialog,
      checkPasswordStrength,
      getPasswordStrengthClass,
      getPasswordStrengthColor,
      checkUsernameAvailability,
      handleRegister,
      goToLogin
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background-color: var(--bg-primary);
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 主要内容区域 */
.register-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  animation: fadeIn var(--transition-normal);
}

.register-card {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--radius-md);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  padding: 40px;
  width: 100%;
  max-width: 500px;
  transition: all var(--transition-normal);
}

.register-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-medium);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header h2 {
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.card-header p {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

/* 表单样式 */
.register-form {
  width: 100%;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}

.password-strength {
  margin-bottom: 24px;
  padding: 16px;
  background-color: var(--bg-secondary);
  border-radius: var(--radius-sm);
}

.password-strength label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}

.strength-text {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  font-weight: 500;
}

.strength-text.weak {
  color: var(--error-color);
}

.strength-text.medium {
  color: var(--warning-color);
}

.strength-text.strong {
  color: var(--success-color);
}

.form-agreement {
  margin-bottom: 24px;
  font-size: 14px;
}

.form-agreement .el-checkbox__label {
  color: var(--text-secondary);
}

.form-agreement .el-link {
  color: var(--primary-color) !important;
  font-weight: 500;
}

.register-btn {
  width: 100%;
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: var(--radius-sm);
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);
  font-family: inherit;
}

.register-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.register-btn:active:not(:disabled) {
  transform: translateY(0);
}

.register-btn:disabled {
  background: var(--bg-tertiary);
  color: var(--text-tertiary);
  cursor: not-allowed;
  box-shadow: none;
}

.card-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
}

.card-footer p {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

.card-footer a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--transition-fast);
}

.card-footer a:hover {
  color: var(--primary-hover);
}

/* 用户名检查样式 */
.username-check {
  display: flex;
  align-items: center;
}

.username-check .success {
  color: var(--success-color);
}

.username-check .error {
  color: var(--error-color);
}

.username-check-message {
  font-size: 12px;
  margin-top: 6px;
}

.username-check-message.success {
  color: var(--success-color);
}

.username-check-message.error {
  color: var(--error-color);
}

/* 成功对话框样式 */
.success-content {
  text-align: center;
  padding: 24px 0;
}

.success-icon {
  margin-bottom: 16px;
  animation: pulse 1.5s infinite;
}

.success-content h3 {
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.success-content p {
  color: var(--text-secondary);
  margin-bottom: 5px;
  line-height: 1.5;
}

.countdown {
  color: var(--primary-color);
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-content {
    padding: 20px 15px;
  }
  
  .register-card {
    padding: 32px 24px;
  }
  
  .card-header h2 {
    font-size: 22px;
  }
}

@media (max-width: 480px) {
  .register-card {
    padding: 24px 20px;
  }
  
  .card-header h2 {
    font-size: 20px;
  }
  
  .password-strength {
    padding: 12px;
  }
}
</style>
