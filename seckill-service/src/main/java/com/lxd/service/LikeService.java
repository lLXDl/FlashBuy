package com.lxd.service;

import com.lxd.config.LikeRabbitConfig;
import com.lxd.entity.LikeMessage;
import com.lxd.mapper.LikeRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final LikeRecordMapper likeRecordMapper;
    private final DefaultRedisScript<Long> likeScript;

    private static final String LIKE_SET_KEY = "like:%s:%s";
    private static final String LIKE_COUNT_KEY = "like:count:%s";
    private static final String USER_LIKE_KEY = "user:like:%s";
    private static final String RANKING_KEY_PREFIX = "ranking:%s";

    // 点赞/取消点赞
    public int toggleLike(Long userId, String targetType, Long targetId, boolean like) {
        String setKey = String.format(LIKE_SET_KEY, targetType, targetId);
        String countKey = String.format(LIKE_COUNT_KEY, targetType);
        String userKey = String.format(USER_LIKE_KEY, userId);
        String rankingKey = String.format(RANKING_KEY_PREFIX, targetType);

        List<String> keys = Arrays.asList(setKey, countKey, userKey, rankingKey);
        List<String> args = Arrays.asList(userId.toString(), targetId.toString(), like ? "like" : "unlike");

        Long result = redisTemplate.execute(likeScript, keys, args.toArray());

        if (result == 1) {
            // 发送消息异步落库
            LikeMessage message = new LikeMessage(userId, targetType, targetId, like ? 1 : 0);
            rabbitTemplate.convertAndSend(LikeRabbitConfig.LIKE_EXCHANGE, LikeRabbitConfig.LIKE_ROUTING_KEY, message);
        }

        return result.intValue();
    }

    // 获取点赞数
    public long getLikeCount(String targetType, Long targetId) {
        String countKey = String.format(LIKE_COUNT_KEY, targetType);
        String count = (String) redisTemplate.opsForHash().get(countKey, targetId.toString());
        return count == null ? 0 : Long.parseLong(count);
    }

    // 判断是否已点赞
    public boolean isLiked(Long userId, String targetType, Long targetId) {
        String setKey = String.format(LIKE_SET_KEY, targetType, targetId);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(setKey, userId.toString()));
    }

    // 共同点赞查询
    public Set<String> getCommonLikes(Long userId1, Long userId2) {
        String key1 = String.format(USER_LIKE_KEY, userId1);
        String key2 = String.format(USER_LIKE_KEY, userId2);
        return redisTemplate.opsForSet().intersect(key1, key2);
    }
}
