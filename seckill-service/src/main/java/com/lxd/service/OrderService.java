package com.lxd.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lxd.entity.Goods;
import com.lxd.entity.Order;
import com.lxd.mapper.GoodsMapper;
import com.lxd.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final GoodsMapper goodsMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 查询用户订单列表
     */
    public List<Map<String, Object>> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.selectList(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime)
        );

        // 查询商品信息
        return orders.stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId().toString());  // 转换为字符串，避免 JavaScript 精度丢失
            map.put("userId", order.getUserId());
            map.put("goodsId", order.getGoodsId());
            map.put("status", order.getStatus());
            map.put("createTime", order.getCreateTime());

            // 查询商品名称
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            if (goods != null) {
                map.put("goodsName", goods.getGoodsName());
                map.put("goodsImage", "https://picsum.photos/300/200?random=" + goods.getId());
                map.put("price", 99.0);  // 默认价格
            } else {
                map.put("goodsName", "未知商品");
                map.put("goodsImage", "https://picsum.photos/300/200");
                map.put("price", 99.0);
            }

            // 状态描述
            String statusText;
            switch (order.getStatus()) {
                case 0:
                    statusText = "待支付";
                    break;
                case 1:
                    statusText = "已支付";
                    break;
                case 2:
                    statusText = "已关闭";
                    break;
                default:
                    statusText = "未知";
            }
            map.put("statusText", statusText);

            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 支付订单
     */
    @Transactional
    public Map<String, Object> payOrder(Long orderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            result.put("success", false);
            result.put("message", "订单不存在");
            return result;
        }

        if (!order.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "无权支付此订单");
            return result;
        }

        if (order.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "订单状态异常，无法支付");
            return result;
        }

        // 更新订单状态为已支付
        int rows = orderMapper.update(
                Wrappers.<Order>lambdaUpdate()
                        .set(Order::getStatus, 1)
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, 0)
        );

        if (rows > 0) {
            result.put("success", true);
            result.put("message", "支付成功");
        } else {
            result.put("success", false);
            result.put("message", "支付失败，请重试");
        }

        return result;
    }

    public boolean closeOrderIfUnpaid(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        // 关闭订单
        int row = orderMapper.update(
                Wrappers.<Order>lambdaUpdate()
                        .set(Order::getStatus, 2)
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, 0)
        );
        if (row == 0) {
            return false;  // 并发已处理
        }
        // 归还库存
        goodsMapper.update(
                Wrappers.<Goods>lambdaUpdate()
                        .setSql("stock = stock + 1")
                        .eq(Goods::getId, order.getGoodsId())
        );

        String key = "seckill:order:" + order.getGoodsId();
        redisTemplate.opsForValue().increment(key, 1L);
        return true;
    }
}
