package com.lxd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用在方法上，表示该方法的结果应被缓存。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {
    String prefix();           // 缓存 key 前缀
    String key() default "";   // SpEL 表达式（动态 key）
    long ttl() default 0;      // 自定义 TTL（0 则使用默认随机 TTL）
}
