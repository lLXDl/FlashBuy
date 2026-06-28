package com.lxd.controller;

import com.lxd.service.LikeService;
import com.lxd.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;
    private final RankingService rankingService;

    // 点赞
    @PostMapping("/{targetType}/{targetId}")
    public Map<String, Object> like(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestParam Long userId) {
        int result = likeService.toggleLike(userId, targetType, targetId, true);
        Map<String, Object> response = new HashMap<>();
        if (result == 1) {
            response.put("success", true);
            response.put("message", "点赞成功");
            response.put("count", likeService.getLikeCount(targetType, targetId));
        } else {
            response.put("success", false);
            response.put("message", "已点赞");
        }
        return response;
    }

    // 取消点赞
    @DeleteMapping("/{targetType}/{targetId}")
    public Map<String, Object> unlike(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestParam Long userId) {
        int result = likeService.toggleLike(userId, targetType, targetId, false);
        Map<String, Object> response = new HashMap<>();
        if (result == 1) {
            response.put("success", true);
            response.put("message", "取消点赞");
            response.put("count", likeService.getLikeCount(targetType, targetId));
        } else {
            response.put("success", false);
            response.put("message", "未点赞");
        }
        return response;
    }

    // 获取点赞数
    @GetMapping("/count/{targetType}/{targetId}")
    public Map<String, Object> getCount(
            @PathVariable String targetType,
            @PathVariable Long targetId) {
        Map<String, Object> response = new HashMap<>();
        response.put("count", likeService.getLikeCount(targetType, targetId));
        return response;
    }

    // 判断是否已点赞
    @GetMapping("/status/{targetType}/{targetId}")
    public Map<String, Object> isLiked(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("liked", likeService.isLiked(userId, targetType, targetId));
        return response;
    }

    // 获取共同点赞
    @GetMapping("/common")
    public Map<String, Object> getCommonLikes(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        Map<String, Object> response = new HashMap<>();
        response.put("commonTargets", likeService.getCommonLikes(userId1, userId2));
        return response;
    }

    // 获取排行榜
    @GetMapping("/ranking/{type}")
    public Map<String, Object> getRanking(
            @PathVariable String type,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> response = new HashMap<>();
        response.put("ranking", rankingService.getTopN(type, limit));
        return response;
    }

    // 获取排名
    @GetMapping("/rank/{type}/{targetId}")
    public Map<String, Object> getRank(
            @PathVariable String type,
            @PathVariable Long targetId) {
        Map<String, Object> response = new HashMap<>();
        Long rank = rankingService.getRank(type, targetId);
        response.put("rank", rank != null ? rank + 1 : -1); // 排名从1开始
        return response;
    }
}