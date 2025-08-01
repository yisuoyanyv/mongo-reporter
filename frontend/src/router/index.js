import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import ReportList from '../views/ReportList.vue'
import ReportDesigner from '../views/ReportDesigner.vue'
import ReportViewer from '../views/ReportViewer.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/register',
      name: 'register',
      component: Register
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: Dashboard,
      meta: { requiresAuth: true }
    },
    {
      path: '/reports',
      name: 'reports',
      component: ReportList,
      meta: { requiresAuth: true }
    },
    {
      path: '/designer',
      name: 'designer',
      component: ReportDesigner,
      meta: { requiresAuth: true }
    },
    {
      path: '/designer/:id',
      name: 'designer-edit',
      component: ReportDesigner,
      meta: { requiresAuth: true }
    },
    {
      path: '/viewer/:id',
      name: 'viewer',
      component: ReportViewer,
      meta: { requiresAuth: true }
    },
    {
      path: '/datasources',
      name: 'datasources',
      component: () => import('../views/DataSourceManager.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('../views/UserManager.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/SystemSettings.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/share/:id',
      name: 'share',
      component: () => import('../views/ReportShare.vue'),
      meta: { requiresAuth: false }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/reports')
  } else {
    next()
  }
})

export default router 