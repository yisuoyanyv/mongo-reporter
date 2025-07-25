import { createRouter, createWebHistory } from 'vue-router'
import ReportDesigner from './views/ReportDesigner.vue'
import DataSourceManager from './views/DataSourceManager.vue'
import ReportList from './views/ReportList.vue'
import Login from './views/Login.vue'
import Register from './views/Register.vue'
import ReportShare from './views/ReportShare.vue'

const routes = [
  { path: '/', redirect: '/designer' },
  { path: '/designer', component: ReportDesigner },
  { path: '/datasource', component: DataSourceManager },
  { path: '/reports', component: ReportList },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/share/:id', component: ReportShare }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const publicPages = ['/login', '/register']
  const authRequired = !publicPages.includes(to.path)
  const token = localStorage.getItem('token')
  if (authRequired && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router 