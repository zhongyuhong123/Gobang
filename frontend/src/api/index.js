import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const service = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true
})

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

service.interceptors.response.use(
  response => {
    const { data } = response
    if (data.code === 200 || data.success === true || data.status === true) {
      return data
    } else {
      if (data.message || data.reason) {
        if (data.message === '用户信息缺失，请重新登录') {
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
        }
        ElMessage.error(data.message || data.reason)
      }
      return Promise.reject(new Error(data.message || data.reason || '请求失败'))
    }
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          ElMessage.error(data.message || data.reason || '请求参数错误')
          break
        case 401:
          ElMessage.error('未授权，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
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
          ElMessage.error(data.message || data.reason || '请求失败')
      }
    } else if (error.request) {
      if (error.code === 'ECONNREFUSED' || error.message?.includes('ECONNREFUSED')) {
        ElMessage.error('服务器连接被拒绝，请检查服务器状态')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (error.message?.includes('Network Error')) {
        ElMessage.error('网络连接失败，请检查网络设置')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else {
        ElMessage.error('网络连接失败，请检查网络设置')
      }
    } else {
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export const userAPI = {
  register: (data) => {
    return service.post('/user/register', {
      username: data.username,
      password: data.password
    })
  },

  login: (data) => {
    return service.post('/user/login', {
      username: data.username,
      password: data.password
    })
  },

  getUserInfo: () => {
    return service.get('/user/info')
  },

  updateUserInfo: (data) => {
    return service.put('/user/update', data)
  },

  getLeaderboard: (type = 'score') => {
    return service.get(`/user/leaderboard/${type}`)
  }
}

export const gameAPI = {
  createRoom: (data) => {
    return service.post('/game/room/create', data)
  },

  getRoomList: () => {
    return service.get('/game/room/list')
  },

  joinRoom: (roomId) => {
    return service.post(`/game/room/join/${roomId}`)
  },

  startMatch: () => {
    return service.post('/game/match/start')
  },

  stopMatch: () => {
    return service.post('/game/match/stop')
  },

  quickMatch: (gameMode) => {
    return service.post('/game/match/quick', { gameMode })
  },

  getGameHistory: () => {
    return service.get('/game/history')
  },

  getGameStats: () => {
    return service.get('/game/stats')
  }
}

export const wsManager = {
  matchSocket: null,
  gameSocket: null,

  connectMatchSocket: (userId, callbacks) => {
    if (wsManager.matchSocket && wsManager.matchSocket.readyState === WebSocket.OPEN) {
      return wsManager.matchSocket
    }

    const token = localStorage.getItem('token')
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = process.env.VUE_APP_WS_BASE_URL ? process.env.VUE_APP_WS_BASE_URL.replace(/^https?:/, protocol) : 'ws://localhost:8080'
    const wsUrl = `${host}/findMatch?userId=${userId}${token ? `&token=${token}` : ''}`
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

  connectGameSocket: (userId, roomId, callbacks) => {
    if (wsManager.gameSocket && wsManager.gameSocket.readyState === WebSocket.OPEN) {
      return wsManager.gameSocket
    }

    const token = localStorage.getItem('token')
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = process.env.VUE_APP_WS_BASE_URL ? process.env.VUE_APP_WS_BASE_URL.replace(/^https?:/, protocol) : 'ws://localhost:8080'
    const wsUrl = `${host}/game?userId=${userId}&roomId=${roomId}${token ? `&token=${token}` : ''}`
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