import { createRouter, createWebHistory } from 'vue-router'

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

router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const isLoggedIn = !!localStorage.getItem('token')
  
  if (requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    next('/home')
  } else {
    next()
  }
})

export default router
