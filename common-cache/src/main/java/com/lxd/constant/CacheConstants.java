package com.lxd.constant;

public class CacheConstants {
    public static final String NULL_CACHE_PREFIX = "null_cache:";
    public static final String LOCK_PREFIX = "cache_lock:";
    public static final long NULL_CACHE_TTL = 300;  // 空对象缓存 5 分钟
    public static final long BASE_TTL = 1800;        // 基础 TTL 30 分钟
    public static final long RANDOM_TTL_RANGE = 300; // 随机 TTL 范围 ±5 分钟
}
