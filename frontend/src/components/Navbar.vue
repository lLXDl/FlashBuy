<template>
  <div class="navbar">
    <div class="navbar-brand">
      <router-link to="/">FlashBuy秒杀系统</router-link>
    </div>
    <div class="navbar-menu">
      <router-link to="/seckill" class="nav-link">秒杀活动</router-link>
      <router-link to="/ranking" class="nav-link">排行榜</router-link>
      <router-link to="/orders" class="nav-link">我的订单</router-link>
    </div>
    <div class="navbar-user">
      <template v-if="userStore.token">
        <span class="username">{{ userStore.userInfo.username }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </template>
      <template v-else>
        <router-link to="/login">
          <el-button type="primary" size="small">登录</el-button>
        </router-link>
      </template>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  await userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-brand a {
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
  text-decoration: none;
}

.navbar-menu {
  display: flex;
  gap: 30px;
}

.nav-link {
  color: #333;
  text-decoration: none;
  font-size: 16px;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #667eea;
}

.nav-link.router-link-active {
  color: #667eea;
  font-weight: bold;
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  color: #666;
  font-size: 14px;
}
</style>