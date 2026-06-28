package com.lxd.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lxd.dto.OrderCreatedEvent;
import com.lxd.dto.SeckillMessage;
import com.lxd.entity.Goods;
import com.lxd.entity.Order;
import com.lxd.mapper.GoodsMapper;
import com.lxd.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeckillService {
    private final GoodsMapper goodsMapper;
    private final OrderMapper orderMapper;
    private final GoodsService goodsService;
    private final CacheService cacheService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(SeckillMessage msg) {
        // 1. 查询商品（走缓存）
        Goods goods = goodsService.getSeckillGoods(msg.getGoodsId());  // 优先查缓存
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }


        // 2. 创建订单
        Order order = new Order();
        order.setUserId(msg.getUserId());
        order.setGoodsId(msg.getGoodsId());
        orderMapper.insert(order);
        Long orderId = order.getId();

        // 3. 扣减库存（必须 stock > 0 才能扣减，防止超卖）
        int success = goodsMapper.update(
                Wrappers.<Goods>lambdaUpdate()
                        .setSql("stock = stock - 1")
                        .eq(Goods::getId, goods.getId())
                        .gt(Goods::getStock, 0));

        if (success == 0) {
            throw new RuntimeException("库存扣减失败");
        }

        // 4. 发送事件（事务提交后触发缓存删除）
        eventPublisher.publishEvent(new OrderCreatedEvent(msg.getGoodsId()));
        return orderId;
    }

    /**
     * 事务提交后删除缓存，保证数据一致性
     */
    @TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        cacheService.delete("seckill:goods:" + event.getGoodsId());
        log.info("订单创建成功，已删除商品缓存，goodsId={}", event.getGoodsId());
    }
}
