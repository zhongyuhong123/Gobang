<template>
  <div class="register-container">
    <div class="register-form">
      <h2>五子棋注册</h2>
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码"></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" class="register-button">注册</el-button>
          <el-button @click="$router.push('/')" class="login-button">返回登录</el-button>
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
  name: 'RegisterView',
  setup() {
    const router = useRouter()
    const registerForm = ref({
      username: '',
      password: '',
      confirmPassword: ''
    })

    const registerFormRef = ref(null)

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

    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== registerForm.value.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
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
  background-color: #f5f5f5;
}

.register-form {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-button {
  width: 100%;
}

.login-button {
  width: 100%;
  margin-top: 10px;
}
</style>
