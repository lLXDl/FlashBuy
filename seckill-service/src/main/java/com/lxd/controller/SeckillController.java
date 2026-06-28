package com.lxd.controller;

import com.lxd.config.RabbitConfig;
import com.lxd.dto.SeckillMessage;
import com.lxd.entity.Goods;
import com.lxd.mapper.GoodsMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/seckill")
@Slf4j
public class SeckillController {

    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final GoodsMapper goodsMapper;

    // Lua 脚本对象
    private final DefaultRedisScript<Long> redisScript;

    public SeckillController(
            StringRedisTemplate stringRedisTemplate,
            RabbitTemplate rabbitTemplate,
            GoodsMapper goodsMapper,
            @Qualifier("seckillScript") DefaultRedisScript<Long> redisScript) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.goodsMapper = goodsMapper;
        this.redisScript = redisScript;
    }

    /**
     * 获取所有秒杀商品列表
     */
    @GetMapping("/goods")
    public List<Goods> getAllGoods() {
        return goodsMapper.selectList(null);
    }

    /**
     * 秒杀接口
     */
    @PostMapping("/{goodsId}")
    public String seckill(
            @PathVariable Long goodsId,
            @RequestParam Long userId) {
        // 自动检查并初始化 Redis 库存数据
        ensureStockInRedis(goodsId);

        // 执行 Lua 脚本（原子操作）
        List<String> keys = Arrays.asList(
                "seckill:stock:" + goodsId,
                "seckill:users:" + goodsId
        );
        Long result = stringRedisTemplate.execute(
                redisScript,
                keys,
                String.valueOf(userId)
        );

        if (result == -3) {
            log.error("库存值格式异常，无法转换为数字");
        }

        if (result != null && result.equals(1L)) {
            // Redis 预减成功，发送消息到 RabbitMQ
            SeckillMessage message = new SeckillMessage(userId, goodsId);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.SECKILL_EXCHANGE,
                    RabbitConfig.SECKILL_ROUTING_KEY,
                    message
            );
            log.info("秒杀请求成功，userId={}, goodsId={}", userId, goodsId);
            return "秒杀请求成功";
        } else if (result != null && result.equals(-1L)) {
            return "库存不足";
        } else if (result != null && result.equals(-2L)) {
            return "您已经参与过秒杀";
        }
        return "系统繁忙";
    }

    /**
     * 确保 Redis 中存在商品库存数据，如果不存在则从数据库同步
     */
    private void ensureStockInRedis(Long goodsId) {
        String stockKey = "seckill:stock:" + goodsId;
        String stock = stringRedisTemplate.opsForValue().get(stockKey);
        
        // 如果 Redis 中没有库存数据，从数据库同步
        if (stock == null) {
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods != null) {
                stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(goods.getStock()));
                log.info("从数据库同步库存到Redis，goodsId={}, stock={}", goodsId, goods.getStock());
            }
        }
    }
}
