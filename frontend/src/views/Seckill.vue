<template>
  <div class="seckill-container">
    <div class="header">
      <h1>限时秒杀</h1>
      <div class="countdown">
        <span>距离结束还剩：</span>
        <span class="time">{{ countdown }}</span>
      </div>
    </div>

    <div class="goods-grid">
      <div v-for="goods in goodsList" :key="goods.id" class="goods-card">
        <div class="goods-image">
          <img :src="goods.image" :alt="goods.name" />
        </div>
        <div class="goods-info">
          <h3>{{ goods.name }}</h3>
          <div class="price">
            <span class="seckill-price">¥{{ goods.seckillPrice }}</span>
            <span class="original-price">¥{{ goods.originalPrice }}</span>
          </div>
          <div class="stock">
            <span>库存：{{ goods.stock }}件</span>
            <el-progress
              :percentage="(goods.stock / goods.totalStock) * 100"
              :color="goods.stock > 10 ? '#67c23a' : '#f56c6c'"
            />
          </div>
          <el-button
            :type="goods.stock > 0 ? 'danger' : 'info'"
            :disabled="goods.stock === 0 || seckillLoading[goods.id]"
            :loading="seckillLoading[goods.id]"
            @click="handleSeckill(goods)"
          >
            {{ goods.stock > 0 ? '立即秒杀' : '已售罄' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 秒杀结果弹窗 -->
    <el-dialog v-model="resultDialogVisible" title="秒杀结果" width="400px">
      <div v-if="seckillResult" class="result-content">
        <el-icon v-if="seckillResult.success" color="#67c23a" :size="60">
          <SuccessFilled />
        </el-icon>
        <el-icon v-else color="#f56c6c" :size="60">
          <CircleCloseFilled />
        </el-icon>
        <p class="result-message">{{ seckillResult.message }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { SuccessFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { seckillGoods, getGoodsList } from '@/api/seckill'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const goodsList = ref([])

// 加载商品列表
const loadGoodsList = async () => {
  try {
    const data = await getGoodsList()
    goodsList.value = data.map(goods => ({
      id: goods.id,
      name: goods.goodsName,
      image: `https://picsum.photos/300/200?random=${goods.id}`,
      seckillPrice: Math.floor(Math.random() * 5000 + 1000),
      originalPrice: Math.floor(Math.random() * 8000 + 2000),
      stock: goods.stock,
      totalStock: goods.stock * 2
    }))
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  }
}

const seckillLoading = reactive({})
const countdown = ref('00:00:00')
const resultDialogVisible = ref(false)
const seckillResult = ref(null)

let timer = null

// 倒计时
const startCountdown = () => {
  const endTime = new Date()
  endTime.setHours(endTime.getHours() + 2) // 2小时后结束

  timer = setInterval(() => {
    const now = new Date()
    const diff = endTime - now

    if (diff <= 0) {
      countdown.value = '00:00:00'
      clearInterval(timer)
      return
    }

    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)

    countdown.value = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  }, 1000)
}

// 秒杀
const handleSeckill = async (goods) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }

  seckillLoading[goods.id] = true

  try {
    const userId = userStore.getUserId()
    const result = await seckillGoods(goods.id, userId)

    seckillResult.value = {
      success: true,
      message: `恭喜！成功抢到 ${goods.name}`
    }

    // 更新库存
    goods.stock--

    // 显示结果弹窗
    resultDialogVisible.value = true
  } catch (error) {
    seckillResult.value = {
      success: false,
      message: error.response?.data?.message || '秒杀失败，请重试'
    }
    resultDialogVisible.value = true
  } finally {
    seckillLoading[goods.id] = false
  }
}

onMounted(() => {
  startCountdown()
  loadGoodsList()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.seckill-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 32px;
  color: #ff4757;
  margin-bottom: 20px;
}

.countdown {
  font-size: 18px;
  color: #666;
}

.countdown .time {
  color: #ff4757;
  font-weight: bold;
  font-size: 24px;
  margin-left: 10px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.goods-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.goods-card:hover {
  transform: translateY(-5px);
}

.goods-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.goods-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.goods-info {
  padding: 20px;
}

.goods-info h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
  height: 40px;
  overflow: hidden;
}

.price {
  margin-bottom: 15px;
}

.seckill-price {
  font-size: 24px;
  color: #ff4757;
  font-weight: bold;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-left: 10px;
}

.stock {
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

.stock .el-progress {
  margin-top: 10px;
}

.goods-info .el-button {
  width: 100%;
  margin-top: 10px;
}

.result-content {
  text-align: center;
  padding: 20px 0;
}

.result-message {
  margin-top: 20px;
  font-size: 18px;
  color: #333;
}
</style>