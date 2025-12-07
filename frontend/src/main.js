import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入游戏主题样式
import './assets/styles/game-theme.css'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
