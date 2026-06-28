package com.lxd.aspect;

import com.lxd.annotation.CacheEvict;
import com.lxd.annotation.Cacheable;
import com.lxd.constant.CacheConstants;
import com.lxd.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {
    private final CacheService cacheService;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(cacheable)")
    public Object aroundCacheable(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        // 1. 获取方法返回类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        // 2. 构建缓存 key
        String cacheKey = buildCacheKey(cacheable.prefix(), cacheable.key(), joinPoint);

        // 3. 尝试从缓存读取（使用方法的返回类型）
        Object cachedValue = cacheService.get(cacheKey, returnType);
        if (cachedValue != null || isNullCache(cacheKey)) {
            return cachedValue;
        }

        // 4. 获取分布式锁，防止缓存击穿
        String lockKey = CacheConstants.LOCK_PREFIX + cacheKey;
        if (cacheService.tryLock(lockKey, 10, 30)) {
            try {
                // 5. 双重检查（防止并发时多个线程重复查库）
                cachedValue = cacheService.get(cacheKey, returnType);
                if (cachedValue != null || isNullCache(cacheKey)) {
                    return cachedValue;
                }

                // 5. 执行目标方法（查库）
                Object result = joinPoint.proceed();

                // 6. 结果处理（防止缓存穿透）
                long ttl = cacheable.ttl() > 0
                        ? cacheable.ttl()
                        : cacheService.getRandomTTL(CacheConstants.BASE_TTL, CacheConstants.RANDOM_TTL_RANGE);

                if (result == null) {
                    // 缓存空对象
                    String nullKey = CacheConstants.NULL_CACHE_PREFIX + cacheKey;
                    cacheService.set(nullKey, null, CacheConstants.NULL_CACHE_TTL);
                } else {
                    // 缓存正常数据
                    cacheService.set(cacheKey, result, ttl);
                }

                return result;
            } finally {
                cacheService.unlock(lockKey);
            }
        } else {
            // ✅ 改进：获取锁失败，短暂休眠后重试查缓存
            try {
                Thread.sleep(50);  // 等待50ms，让获取锁的线程有时间查库写缓存
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 再次尝试从缓存读取（此时持有锁的线程大概率已经写完缓存）
            cachedValue = cacheService.get(cacheKey, returnType);
            if (cachedValue != null || isNullCache(cacheKey)) {
                return cachedValue;
            }

            // 如果缓存依然没有，可能是持有锁的线程还没写完，再次重试
            // 这里可以递归重试，但注意不要无限循环
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            cachedValue = cacheService.get(cacheKey, returnType);
            if (cachedValue != null || isNullCache(cacheKey)) {
                return cachedValue;
            }

            // 最终降级：查库（此时概率已经很低）
            return joinPoint.proceed();
        }
    }

    @After("@annotation(cacheEvict)")
    public void afterCacheEvict(JoinPoint joinPoint, CacheEvict cacheEvict) {
        String cacheKey = buildCacheKey(cacheEvict.prefix(), cacheEvict.key(), joinPoint);
        cacheService.delete(cacheKey);
    }

    private String buildCacheKey(String prefix, String keySpel, JoinPoint joinPoint) {
        if (keySpel.isEmpty()) {
            return prefix;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        String key = parser.parseExpression(keySpel).getValue(context, String.class);
        return prefix + ":" + key;
    }

    private boolean isNullCache(String cacheKey) {
        String nullKey = CacheConstants.NULL_CACHE_PREFIX + cacheKey;
        return cacheService.get(nullKey, String.class) != null;
    }
}
