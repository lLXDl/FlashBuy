<template>
  <div class="ranking-container">
    <h1>商品热度排行榜</h1>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="商品排行" name="goods">
        <el-table :data="rankingList" stripe style="width: 100%">
          <el-table-column type="index" label="排名" width="80" />
          <el-table-column prop="goodsName" label="商品名称" width="200" />
          <el-table-column prop="score" label="点赞数" width="120" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button
                :type="likedStatus[row.targetId] ? 'primary' : 'default'"
                :icon="likedStatus[row.targetId] ? 'StarFilled' : 'Star'"
                @click="toggleLike(row.targetId)"
              >
                {{ likedStatus[row.targetId] ? '已点赞' : '点赞' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- <el-tab-pane label="用户排行" name="user">
        <el-table :data="rankingList" stripe style="width: 100%">
          <el-table-column type="index" label="排名" width="80" />
          <el-table-column prop="targetId" label="用户ID" width="120" />
          <el-table-column prop="count" label="点赞数" width="120" />
        </el-table>
      </el-tab-pane> -->
    </el-tabs>

    <div v-loading="loading" class="loading-container"></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRanking, like, unlike, checkLikeStatus } from '@/api/like'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const activeTab = ref('goods')
const rankingList = ref([])
const loading = ref(false)
const likedStatus = reactive({})

// 获取排行榜
const fetchRanking = async () => {
  loading.value = true
  try {
    const response = await getRanking(activeTab.value, 20)
    rankingList.value = response.ranking || []

    // 检查每个商品的点赞状态
    if (activeTab.value === 'goods') {
      for (const item of rankingList.value) {
        const status = await checkLikeStatus('goods', item.targetId, userStore.getUserId())
        likedStatus[item.targetId] = status.liked
      }
    }
  } catch (error) {
    ElMessage.error('获取排行榜失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 切换标签
const handleTabChange = () => {
  fetchRanking()
}

// 点赞/取消点赞
const toggleLike = async (targetId) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    const userId = userStore.getUserId()

    if (likedStatus[targetId]) {
      await unlike('goods', targetId, userId)
      likedStatus[targetId] = false
      ElMessage.success('取消点赞')
    } else {
      await like('goods', targetId, userId)
      likedStatus[targetId] = true
      ElMessage.success('点赞成功')
    }

    // 重新获取排行榜
    await fetchRanking()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

onMounted(() => {
  fetchRanking()
})
</script>

<style scoped>
.ranking-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

.loading-container {
  min-height: 200px;
}

.el-button {
  margin-right: 10px;
}
</style>