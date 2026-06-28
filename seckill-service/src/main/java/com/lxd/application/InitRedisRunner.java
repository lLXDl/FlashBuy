package com.lxd.application;

import com.lxd.entity.Goods;
import com.lxd.entity.LikeRecord;
import com.lxd.mapper.GoodsMapper;
import com.lxd.mapper.LikeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitRedisRunner implements CommandLineRunner {
    private final StringRedisTemplate stringRedisTemplate;
    private final GoodsMapper goodsMapper;
    private final LikeRecordMapper likeRecordMapper;


    @Override
    public void run(String... args) throws Exception {
        log.info("========== 开始初始化 Redis 数据 ==========");
        
        try {
            // 测试 Redis 连接
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            log.info("Redis 连接成功: {}", pong);
        } catch (Exception e) {
            log.error("Redis 连接失败: {}", e.getMessage());
            return;
        }

        // 初始化库存数据
        initStockData();

        // 初始化排行榜数据
        initRankingData();
        
        log.info("========== Redis 数据初始化完成 ==========");
    }

    // 初始化库存数据
    private void initStockData() {
        List<Goods> goods = goodsMapper.selectList(null);
        log.info("从数据库查询到 {} 个商品", goods.size());
        
        for (Goods good : goods) {
            String stockKey = "seckill:stock:" + good.getId();
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(good.getStock()));
            log.info("设置库存: {} = {}", stockKey, good.getStock());
            
            // 清空用户集合（重启时重置）
            stringRedisTemplate.delete("seckill:users:" + good.getId());
        }
    }

    // 初始化排行榜数据
    private void initRankingData() {
        // 查询所有有效的点赞记录（status=1 表示已点赞）
        List<LikeRecord> likeRecords = likeRecordMapper.selectList(null);
        log.info("从数据库查询到 {} 条点赞记录", likeRecords.size());

        // 按 targetType 和 targetId 分组统计点赞数
        Map<String, Map<Long, Long>> likeCountMap = likeRecords.stream()
                .filter(record -> record.getStatus() != null && record.getStatus() == 1)
                .collect(Collectors.groupingBy(
                        LikeRecord::getTargetType,
                        Collectors.groupingBy(
                                LikeRecord::getTargetId,
                                Collectors.counting()
                        )
                ));

        // 将统计结果写入 Redis 排行榜
        for (Map.Entry<String, Map<Long, Long>> typeEntry : likeCountMap.entrySet()) {
            String targetType = typeEntry.getKey();
            String rankingKey = "ranking:" + targetType;
            
            for (Map.Entry<Long, Long> idEntry : typeEntry.getValue().entrySet()) {
                Long targetId = idEntry.getKey();
                Long count = idEntry.getValue();
                
                stringRedisTemplate.opsForZSet().add(rankingKey, targetId.toString(), count);
                log.info("设置排行榜: {} {} = {}", rankingKey, targetId, count);
            }
        }
    }
}
