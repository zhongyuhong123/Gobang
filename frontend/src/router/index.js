import { createRouter, createWebHistory } from 'vue-router'

// 路由懒加载
const WelcomeView = () => import('../views/WelcomeView.vue')
const LoginView = () => import('../views/LoginView.vue')
const RegisterView = () => import('../views/RegisterView.vue')
const HomeView = () => import('../views/HomeView.vue')
const GameView = () => import('../views/GameView.vue')

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
  // 如果用户已登录但访问的是登录或注册页面，重定向到游戏大厅
  else if (isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    next('/home')
  }
  // 其他情况正常导航
  else {
    next()
  }
})

export default router
