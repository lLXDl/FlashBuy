import request from '@/utils/request'

// 点赞
export function like(targetType, targetId, userId) {
  return request({
    url: `/like/${targetType}/${targetId}`,
    method: 'post',
    params: { userId }
  })
}

// 取消点赞
export function unlike(targetType, targetId, userId) {
  return request({
    url: `/like/${targetType}/${targetId}`,
    method: 'delete',
    params: { userId }
  })
}

// 获取点赞数
export function getLikeCount(targetType, targetId) {
  return request({
    url: `/like/count/${targetType}/${targetId}`,
    method: 'get'
  })
}

// 检查是否已点赞
export function checkLikeStatus(targetType, targetId, userId) {
  return request({
    url: `/like/status/${targetType}/${targetId}`,
    method: 'get',
    params: { userId }
  })
}

// 获取排行榜
export function getRanking(type, limit = 10) {
  return request({
    url: `/like/ranking/${type}`,
    method: 'get',
    params: { limit }
  })
}

// 获取排名
export function getRank(type, targetId) {
  return request({
    url: `/like/rank/${type}/${targetId}`,
    method: 'get'
  })
}