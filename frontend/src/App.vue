<template>
  <div id="app">
    <!-- 液态玻璃背景 -->
    <div class="glass-background">
      <div class="glass-bubble glass-bubble-1"></div>
      <div class="glass-bubble glass-bubble-2"></div>
      <div class="glass-bubble glass-bubble-3"></div>
      <div class="glass-bubble glass-bubble-4"></div>
      <div class="glass-bubble glass-bubble-5"></div>
    </div>
    <!-- 背景图案 -->
    <div class="bg-pattern"></div>
    <!-- 主要内容区域 - 添加页面过渡效果 -->
    <router-view v-slot="{ Component }">
      <transition name="page-fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Arial', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: var(--text-primary);
  min-height: 100vh;
  position: relative;
}

/* 页面过渡动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.3s ease;
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}

/* 液态玻璃背景动画 */
.glass-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -2;
  overflow: hidden;
}

.glass-bubble {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: float-glass 20s infinite ease-in-out;
}

.glass-bubble-1 {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
  animation-duration: 25s;
}

.glass-bubble-2 {
  width: 120px;
  height: 120px;
  top: 60%;
  left: 80%;
  animation-delay: -5s;
  animation-duration: 30s;
}

.glass-bubble-3 {
  width: 60px;
  height: 60px;
  top: 80%;
  left: 30%;
  animation-delay: -10s;
  animation-duration: 20s;
}

.glass-bubble-4 {
  width: 100px;
  height: 100px;
  top: 40%;
  left: 60%;
  animation-delay: -15s;
  animation-duration: 35s;
}

.glass-bubble-5 {
  width: 40px;
  height: 40px;
  top: 10%;
  left: 70%;
  animation-delay: -20s;
  animation-duration: 22s;
}

@keyframes float-glass {
  0%, 100% {
    transform: translateY(0) translateX(0) rotate(0deg);
  }
  25% {
    transform: translateY(-30px) translateX(20px) rotate(90deg);
  }
  50% {
    transform: translateY(20px) translateX(-10px) rotate(180deg);
  }
  75% {
    transform: translateY(-20px) translateX(-15px) rotate(270deg);
  }
}
</style>
