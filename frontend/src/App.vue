<template>
  <div id="app">
    <!-- 背景图案 -->
    <div class="bg-pattern"></div>
    <!-- 主要内容区域 -->
    <router-view />
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
</style>
