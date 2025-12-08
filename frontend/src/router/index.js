import { createRouter, createWebHistory } from 'vue-router'
import WelcomeView from '../views/WelcomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import HomeView from '../views/HomeView.vue'
import GameView from '../views/GameView.vue'

const routes = [
  {
    path: '/',
    name: 'welcome',
    component: WelcomeView,
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { requiresAuth: false }
  },
  {
    path: '/home',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: true }
  },
  {
    path: '/game/:roomId?',
    name: 'game',
    component: GameView,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 全局路由守卫 - 登录状态检测
router.beforeEach((to, from, next) => {
  // 检查是否需要登录
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const isLoggedIn = !!localStorage.getItem('token')
  
  // 如果路由需要登录但用户未登录，重定向到登录页面
  if (requiresAuth && !isLoggedIn) {
    next('/login')
  } 
  // 如果用户已登录但访问的是登录或注册页面，重定向到首页
  else if (isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    next('/home')
  }
  // 如果用户已登录且访问的是欢迎页面，重定向到首页
  else if (isLoggedIn && to.path === '/') {
    next('/home')
  }
  // 其他情况正常导航
  else {
    next()
  }
})

export default router
