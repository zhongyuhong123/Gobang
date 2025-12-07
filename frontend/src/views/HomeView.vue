<template>
  <div class="home-container">
    <header class="header">
      <h1>五子棋游戏大厅</h1>
      <div class="user-info">
        <span>欢迎，{{ user ? user.username : '游客' }}</span>
        <el-button @click="handleLogout">退出登录</el-button>
      </div>
    </header>
    <main class="main-content">
      <el-button type="primary" @click="findMatch" class="match-button">开始匹配</el-button>
      <el-button @click="createRoom" class="create-button">创建房间</el-button>
      <el-button @click="joinRoom" class="join-button">加入房间</el-button>
    </main>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'HomeView',
  setup() {
    const router = useRouter()
    const user = ref(null)

    onMounted(() => {
      const userInfo = localStorage.getItem('user')
      if (userInfo) {
        user.value = JSON.parse(userInfo)
      } else {
        router.push('/')
      }
    })

    const handleLogout = () => {
      localStorage.removeItem('user')
      router.push('/')
      ElMessage.success('已退出登录')
    }

    const findMatch = () => {
      // TODO: 实现匹配功能
      ElMessage.info('匹配功能开发中')
      // 临时直接进入游戏页面
      router.push('/game')
    }

    const createRoom = () => {
      // TODO: 实现创建房间功能
      ElMessage.info('创建房间功能开发中')
      // 临时直接进入游戏页面
      router.push('/game')
    }

    const joinRoom = () => {
      // TODO: 实现加入房间功能
      ElMessage.info('加入房间功能开发中')
      // 临时直接进入游戏页面
      router.push('/game')
    }

    return {
      user,
      handleLogout,
      findMatch,
      createRoom,
      joinRoom
    }
  }
}
</script>

<style scoped>
.home-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #409eff;
  color: white;
  height: 60px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-info span {
  margin-right: 10px;
}

.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  background-color: #f5f5f5;
}

.match-button,
.create-button,
.join-button {
  width: 150px;
  height: 50px;
  font-size: 18px;
}
</style>
