<template>
  <div class="register-container">
    <div class="bg-pattern"></div>
    <div class="register-form game-card">
      <div class="logo">
        <div class="chess-piece"></div>
      </div>
      <h2 class="game-title">Come 下棋</h2>
        <h3 class="subtitle">创建新账号，开启棋艺之旅</h3>
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-position="top">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名" prefix-icon="User"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码" prefix-icon="Lock"></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" class="game-button register-button">注册</el-button>
        </el-form-item>
        <div class="login-link">
          <span>已有账号？</span>
          <el-button type="text" @click="$router.push('/')" class="login-button">立即登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'RegisterView',
  setup() {
    const router = useRouter()
    const registerForm = ref({
      username: '',
      password: '',
      confirmPassword: ''
    })

    const registerFormRef = ref(null)

    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== registerForm.value.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }

    const handleRegister = async () => {
      try {
        const valid = await registerFormRef.value.validate()
        if (valid) {
          const response = await axios.post('http://localhost:8080/register', {
            username: registerForm.value.username,
            password: registerForm.value.password
          })
          if (response.data.status) {
            ElMessage.success('注册成功')
            router.push('/')
          } else {
            ElMessage.error(response.data.message)
          }
        }
      } catch (error) {
        console.error('注册失败:', error)
        ElMessage.error('注册失败，请重试')
      }
    }

    return {
      registerForm,
      registerFormRef,
      registerRules,
      handleRegister
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
  position: relative;
  overflow: hidden;
}

.register-form {
  width: 400px;
  max-width: 90%;
  position: relative;
  z-index: 1;
}

.logo {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.chess-piece {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, var(--black-stone), var(--white-stone));
  box-shadow: 0 0 30px rgba(0, 0, 0, 0.5), 0 0 60px rgba(82, 196, 26, 0.3);
  animation: pulse 2s infinite;
  border: 3px solid var(--success-color);
}

.subtitle {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-weight: normal;
}

.register-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
}

.login-link {
  margin-top: 1.5rem;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 14px;
}

.login-link span {
  margin-right: 8px;
}

.login-button {
  color: var(--primary-color) !important;
  font-weight: bold !important;
  padding: 0 !important;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-form {
    padding: 1.5rem;
  }
  
  .chess-piece {
    width: 50px;
    height: 50px;
  }
}
</style>
