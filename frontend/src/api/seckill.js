import request from '@/utils/request'

// 秒杀商品
export function seckillGoods(goodsId, userId) {
  return request({
    url: `/seckill/${goodsId}`,
    method: 'post',
    params: { userId }
  })
}

// 获取商品列表（模拟数据，实际需要后端提供接口）
export function getGoodsList() {
  return request({
    url: '/seckill/goods',
    method: 'get'
  })
}