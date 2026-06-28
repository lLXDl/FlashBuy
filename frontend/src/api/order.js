import request from '@/utils/request'

// 查询用户订单列表
export function getOrderList(userId) {
  return request({
    url: '/order/list',
    method: 'get',
    params: { userId }
  })
}

// 支付订单
export function payOrder(orderId, userId) {
  return request({
    url: `/order/pay/${orderId}`,
    method: 'post',
    params: { userId }
  })
}