<template>
  <div class="register-container">
    <div class="register-form">
      <div class="form-header">
        <h2>用户注册</h2>
        <p class="form-subtitle">创建您的五子棋游戏账户</p>
      </div>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        label-width="100px"
        size="large"
        class="register-form-content"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
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
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
            @input="checkPasswordStrength"
          />
        </el-form-item>

        <el-form-item v-if="passwordStrength.text" label="密码强度">
          <div class="password-strength">
            <el-progress 
              :percentage="passwordStrength.percentage" 
              :color="getPasswordStrengthColor()"
              :show-text="false"
              stroke-width="8"
            />
            <span class="strength-text" :class="getPasswordStrengthClass()">
              {{ passwordStrength.text }}
            </span>
          </div>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="用户协议" prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <el-link type="primary" :underline="false">用户协议</el-link>
            和
            <el-link type="primary" :underline="false">隐私政策</el-link>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleRegister" 
            :loading="loading"
            size="large"
            style="width: 100%"
          >
            立即注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="form-footer">
        <div class="login-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
        <div class="other-links">
          <router-link to="/forgot-password">忘记密码？</router-link>
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
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { userAPI } from '../api/index.js'

export default {
  name: 'RegisterView',
  components: {
    CircleCheck,
    CircleClose
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
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.register-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(255,255,255,0.1) 2px, transparent 2px),
    radial-gradient(circle at 75% 75%, rgba(255,255,255,0.1) 2px, transparent 2px);
  background-size: 50px 50px;
  animation: patternMove 20s linear infinite;
}

@keyframes patternMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

.register-form {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 480px;
  z-index: 1;
  position: relative;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.form-subtitle {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

.register-form-content {
  margin-bottom: 20px;
}

.username-check {
  display: flex;
  align-items: center;
  margin-left: 8px;
}

.username-check .el-icon {
  font-size: 16px;
}

.username-check .el-icon.success {
  color: #67c23a;
}

.username-check .el-icon.error {
  color: #f56c6c;
}

.username-check-message {
  font-size: 12px;
  margin-top: 4px;
}

.username-check-message.success {
  color: #67c23a;
}

.username-check-message.error {
  color: #f56c6c;
}

.password-strength {
  width: 100%;
}

.strength-text {
  font-size: 12px;
  margin-top: 8px;
  display: block;
}

.strength-text.weak {
  color: #f56c6c;
}

.strength-text.medium {
  color: #e6a23c;
}

.strength-text.strong {
  color: #67c23a;
}

.form-footer {
  text-align: center;
  color: #7f8c8d;
  font-size: 14px;
}

.login-link {
  margin-bottom: 10px;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.login-link a:hover {
  color: #764ba2;
  text-decoration: underline;
}

.other-links a {
  color: #7f8c8d;
  text-decoration: none;
  transition: color 0.3s ease;
}

.other-links a:hover {
  color: #667eea;
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 20px;
}

.success-content h3 {
  font-size: 20px;
  font-weight: 500;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.success-content p {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0 0 8px 0;
}

.countdown {
  color: #667eea;
  font-weight: 600;
  font-size: 16px;
}

.dialog-footer {
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-form {
    margin: 20px;
    padding: 30px 20px;
  }
  
  .form-header h2 {
    font-size: 24px;
  }
  
  .register-form-content {
    margin-bottom: 15px;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .register-form {
    background: rgba(30, 30, 30, 0.95);
    color: #e0e0e0;
  }
  
  .form-header h2 {
    background: linear-gradient(135deg, #8a9eff 0%, #a78bfa 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .form-subtitle {
    color: #b0b0b0;
  }
}
</style>
