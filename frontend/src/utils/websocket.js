// WebSocket管理器，用于处理游戏连接

const wsManager = {
  // 已连接的WebSocket实例
  connections: new Map(),
  
  /**
   * 连接游戏WebSocket
   * @param {string} userId - 用户ID
   * @param {string|number} roomId - 房间ID
   * @param {Object} callbacks - 回调函数
   * @returns {WebSocket} WebSocket实例
   */
  connectGameSocket(userId, roomId, callbacks) {
    // 构建WebSocket URL（根据实际后端地址配置）
    const wsUrl = `ws://localhost:8080/api/game/${roomId}?userId=${userId}`;
    
    try {
      const socket = new WebSocket(wsUrl);
      
      // 存储连接
      const connectionKey = `${userId}-${roomId}`;
      this.connections.set(connectionKey, socket);
      
      // 设置回调
      if (callbacks.onOpen) {
        socket.onopen = callbacks.onOpen;
      }
      
      if (callbacks.onMessage) {
        socket.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            callbacks.onMessage(data);
          } catch (error) {
            console.error('解析WebSocket消息失败:', error);
          }
        };
      }
      
      if (callbacks.onError) {
        socket.onerror = callbacks.onError;
      }
      
      if (callbacks.onClose) {
        socket.onclose = callbacks.onClose;
      }
      
      return socket;
    } catch (error) {
      console.error('创建WebSocket连接失败:', error);
      if (callbacks.onError) {
        callbacks.onError(error);
      }
      return null;
    }
  },
  
  /**
   * 发送消息
   * @param {WebSocket} socket - WebSocket实例
   * @param {Object} data - 消息数据
   */
  sendMessage(socket, data) {
    if (socket && socket.readyState === WebSocket.OPEN) {
      try {
        socket.send(JSON.stringify(data));
        return true;
      } catch (error) {
        console.error('发送WebSocket消息失败:', error);
        return false;
      }
    }
    return false;
  },
  
  /**
   * 关闭指定连接
   * @param {string} userId - 用户ID
   * @param {string|number} roomId - 房间ID
   */
  closeConnection(userId, roomId) {
    const connectionKey = `${userId}-${roomId}`;
    const socket = this.connections.get(connectionKey);
    
    if (socket) {
      socket.close();
      this.connections.delete(connectionKey);
      return true;
    }
    return false;
  },
  
  /**
   * 关闭所有连接
   */
  closeAll() {
    this.connections.forEach((socket, key) => {
      socket.close();
      this.connections.delete(key);
    });
  }
};

export default wsManager;