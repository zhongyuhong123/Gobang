<template>
  <div class="login-container">
    <div class="login-form">
      <h2>五子棋登录</h2>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-button">登录</el-button>
          <el-button @click="$router.push('/register')" class="register-button">注册</el-button>
        </el-form-item>
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
  background-color: #f5f5f5;
}

.login-form {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-button {
  width: 100%;
}

.register-button {
  width: 100%;
  margin-top: 10px;
}
</style>
