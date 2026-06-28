<template>
  <div class="orders-container">
    <h1>我的订单</h1>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部订单" name="all">
        <el-table :data="orderList" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="订单号" width="180" />
          <el-table-column label="商品" width="300">
            <template #default="{ row }">
              <div class="goods-info">
                <img :src="`https://picsum.photos/60/40?random=${row.id}`" :alt="row.goodsName" class="goods-image" />
                <span>{{ row.goodsName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="120">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="statusText" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ row.statusText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 0"
                type="primary"
                size="small"
                @click="handlePay(row)"
                :loading="payLoading[row.id]"
              >
                立即支付
              </el-button>
              <el-button
                v-if="row.status === 1"
                type="success"
                size="small"
                plain
              >
                已完成
              </el-button>
              <el-button
                v-if="row.status === 2"
                type="info"
                size="small"
                plain
              >
                已关闭
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="待支付" name="pending">
        <el-table :data="pendingOrders" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="订单号" width="180" />
          <el-table-column label="商品" width="300">
            <template #default="{ row }">
              <div class="goods-info">
                <img :src="row.goodsImage" :alt="row.goodsName" class="goods-image" />
                <span>{{ row.goodsName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="120">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handlePay(row)"
                :loading="payLoading[row.id]"
              >
                立即支付
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="已支付" name="paid">
        <el-table :data="paidOrders" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="订单号" width="180" />
          <el-table-column label="商品" width="300">
            <template #default="{ row }">
              <div class="goods-info">
                <img :src="row.goodsImage" :alt="row.goodsName" class="goods-image" />
                <span>{{ row.goodsName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="120">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="success" size="small" plain>
                已完成
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-empty v-if="!loading && orderList.length === 0" description="暂无订单" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrderList, payOrder } from '@/api/order'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const activeTab = ref('all')
const orderList = ref([])
const loading = ref(false)
const payLoading = reactive({})

// 待支付订单
const pendingOrders = computed(() => {
  return orderList.value.filter(order => order.status === 0)
})

// 已支付订单
const paidOrders = computed(() => {
  return orderList.value.filter(order => order.status === 1)
})

// 加载订单列表
const loadOrders = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const userId = userStore.getUserId()
    const data = await getOrderList(userId)
    orderList.value = data || []
  } catch (error) {
    ElMessage.error('加载订单列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 切换标签
const handleTabChange = () => {
  // 标签切换时不需要重新加载，数据已经分类好了
}

// 支付订单
const handlePay = async (order) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }

  payLoading[order.id] = true
  try {
    const userId = userStore.getUserId()
    const result = await payOrder(order.id, userId)

    if (result.success) {
      ElMessage.success('支付成功')
      // 更新订单状态
      order.status = 1
      order.statusText = '已支付'
    } else {
      ElMessage.error(result.message || '支付失败')
    }
  } catch (error) {
    ElMessage.error('支付失败，请重试')
    console.error(error)
  } finally {
    payLoading[order.id] = false
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'info'
    default:
      return ''
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

.goods-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.goods-image {
  width: 60px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.el-button {
  margin-right: 10px;
}
</style>