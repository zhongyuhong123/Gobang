<template>
  <div class="login-container">
    <div class="bg-pattern"></div>
    <div class="login-form game-card">
      <div class="logo">
        <div class="chess-piece"></div>
      </div>
      <h2 class="game-title">Come 下棋</h2>
        <h3 class="subtitle">支持五子棋、军棋、象棋等多种棋类游戏</h3>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-position="top">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="game-button login-button">登录</el-button>
        </el-form-item>
        <div class="register-link">
          <span>还没有账号？</span>
          <el-button type="text" @click="$router.push('/register')" class="register-button">立即注册</el-button>
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
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const loginForm = ref({
      username: '',
      password: ''
    })

    const loginFormRef = ref(null)

    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ]
    }

    const handleLogin = async () => {
      try {
        const valid = await loginFormRef.value.validate()
        if (valid) {
          const response = await axios.post('http://localhost:8080/login', loginForm.value)
          if (response.data.status) {
            ElMessage.success('登录成功')
            localStorage.setItem('user', JSON.stringify(response.data.user))
            router.push('/home')
          } else {
            ElMessage.error(response.data.message)
          }
        }
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('登录失败，请重试')
      }
    }

    return {
      loginForm,
      loginFormRef,
      loginRules,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
  position: relative;
  overflow: hidden;
}

.login-form {
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
  background: radial-gradient(circle at 30% 30%, var(--white-stone), var(--black-stone));
  box-shadow: 0 0 30px rgba(0, 0, 0, 0.5), 0 0 60px rgba(24, 144, 255, 0.3);
  animation: pulse 2s infinite;
  border: 3px solid var(--primary-color);
}

.subtitle {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-weight: normal;
}

.login-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
}

.register-link {
  margin-top: 1.5rem;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 14px;
}

.register-link span {
  margin-right: 8px;
}

.register-button {
  color: var(--primary-color) !important;
  font-weight: bold !important;
  padding: 0 !important;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-form {
    padding: 1.5rem;
  }
  
  .chess-piece {
    width: 50px;
    height: 50px;
  }
}
</style>
