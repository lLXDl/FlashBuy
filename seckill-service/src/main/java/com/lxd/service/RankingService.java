package com.lxd.service;

import com.lxd.entity.Goods;
import com.lxd.entity.RankingSnapshot;
import com.lxd.mapper.GoodsMapper;
import com.lxd.mapper.RankingSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RankingService {
    private final StringRedisTemplate redisTemplate;
    private final RankingSnapshotMapper rankingSnapshotMapper;
    private final GoodsMapper goodsMapper;

    private static final String RANKING_KEY = "ranking:%s";

    // 获取排行榜前N名
    public List<Map<String, Object>> getTopN(String type, int limit) {
        String key = String.format(RANKING_KEY, type);
        Set<ZSetOperations.TypedTuple<String>> top = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, limit - 1);

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : top) {
            Map<String, Object> item = new HashMap<>();
            Long targetId = Long.parseLong(tuple.getValue());
            item.put("targetId", targetId);
            item.put("score", tuple.getScore().intValue());
            item.put("rank", rank++);
            
            if ("goods".equals(type)) {
                Goods goods = goodsMapper.selectById(targetId);
                item.put("goodsName", goods != null ? goods.getGoodsName() : "未知商品");
            }
            
            result.add(item);
        }
        return result;
    }

    // 获取某个目标的排名
    public Long getRank(String type, Long targetId) {
        String key = String.format(RANKING_KEY, type);
        return redisTemplate.opsForZSet().reverseRank(key, targetId.toString());
    }

    // 定时快照到数据库
    public void snapshot(String type) {
        String key = String.format(RANKING_KEY, type);
        Set<ZSetOperations.TypedTuple<String>> all = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, -1);

        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : all) {
            RankingSnapshot snapshot = new RankingSnapshot();
            snapshot.setRankingType(type);
            snapshot.setTargetId(Long.parseLong(tuple.getValue()));
            snapshot.setScore(tuple.getScore().intValue());
            snapshot.setRank(rank++);
            rankingSnapshotMapper.upsert(snapshot);
        }
    }
}