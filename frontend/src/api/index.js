import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const { data } = response
    if (data.code === 200 || data.success === true) {
      return data
    } else {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          router.push('/login')
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络设置')
    } else {
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

// 用户相关API
export const userAPI = {
  // 用户注册
  register: (data) => {
    return service.post('/user/register', {
      username: data.username,
      password: data.password
    })
  },

  // 用户登录
  login: (data) => {
    return service.post('/user/login', {
      username: data.username,
      password: data.password
    })
  },

  // 获取用户信息
  getUserInfo: () => {
    return service.get('/user/info')
  },

  // 更新用户信息
  updateUserInfo: (data) => {
    return service.put('/user/update', data)
  },

  // 获取排行榜
  getLeaderboard: (type = 'score') => {
    return service.get(`/user/leaderboard/${type}`)
  }
}

// 游戏相关API
export const gameAPI = {
  // 创建房间
  createRoom: (data) => {
    return service.post('/game/room/create', data)
  },

  // 获取房间列表
  getRoomList: () => {
    return service.get('/game/room/list')
  },

  // 加入房间
  joinRoom: (roomId) => {
    return service.post(`/game/room/join/${roomId}`)
  },

  // 开始匹配
  startMatch: () => {
    return service.post('/game/match/start')
  },

  // 停止匹配
  stopMatch: () => {
    return service.post('/game/match/stop')
  },

  // 获取游戏记录
  getGameHistory: () => {
    return service.get('/game/history')
  }
}

// WebSocket管理
export const wsManager = {
  // 游戏大厅WebSocket
  matchSocket: null,
  
  // 游戏房间WebSocket
  gameSocket: null,

  // 连接匹配WebSocket
  connectMatchSocket: (userId, callbacks) => {
    if (wsManager.matchSocket && wsManager.matchSocket.readyState === WebSocket.OPEN) {
      return wsManager.matchSocket
    }

    // 根据后端WebSocket配置，匹配WebSocket连接路径为/findMatch
    const wsUrl = `${process.env.VUE_APP_WS_BASE_URL || 'ws://localhost:8080'}/findMatch?userId=${userId}`
    wsManager.matchSocket = new WebSocket(wsUrl)

    wsManager.matchSocket.onopen = () => {
      console.log('匹配WebSocket连接已建立')
      callbacks?.onOpen && callbacks.onOpen()
    }

    wsManager.matchSocket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        callbacks?.onMessage && callbacks.onMessage(data)
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }

    wsManager.matchSocket.onerror = (error) => {
      console.error('匹配WebSocket错误:', error)
      callbacks?.onError && callbacks.onError(error)
    }

    wsManager.matchSocket.onclose = () => {
      console.log('匹配WebSocket连接已关闭')
      wsManager.matchSocket = null
      callbacks?.onClose && callbacks.onClose()
    }

    return wsManager.matchSocket
  },

  // 连接游戏WebSocket
  connectGameSocket: (userId, roomId, callbacks) => {
    if (wsManager.gameSocket && wsManager.gameSocket.readyState === WebSocket.OPEN) {
      return wsManager.gameSocket
    }

    // 根据后端WebSocket配置，游戏WebSocket连接路径为/game
    const wsUrl = `${process.env.VUE_APP_WS_BASE_URL || 'ws://localhost:8080'}/game?userId=${userId}&roomId=${roomId}`
    wsManager.gameSocket = new WebSocket(wsUrl)

    wsManager.gameSocket.onopen = () => {
      console.log('游戏WebSocket连接已建立')
      callbacks?.onOpen && callbacks.onOpen()
    }

    wsManager.gameSocket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        callbacks?.onMessage && callbacks.onMessage(data)
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }

    wsManager.gameSocket.onerror = (error) => {
      console.error('游戏WebSocket错误:', error)
      callbacks?.onError && callbacks.onError(error)
    }

    wsManager.gameSocket.onclose = () => {
      console.log('游戏WebSocket连接已关闭')
      wsManager.gameSocket = null
      callbacks?.onClose && callbacks.onClose()
    }

    return wsManager.gameSocket
  },

  // 关闭所有WebSocket连接
  closeAll: () => {
    if (wsManager.matchSocket) {
      wsManager.matchSocket.close()
      wsManager.matchSocket = null
    }
    if (wsManager.gameSocket) {
      wsManager.gameSocket.close()
      wsManager.gameSocket = null
    }
  },

  // 发送消息
  sendMessage: (socket, message) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify(message))
      return true
    } else {
      console.error('WebSocket连接未建立或已关闭')
      return false
    }
  }
}

export default service