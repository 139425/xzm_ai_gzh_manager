package com.xzm.xzm_ai_gzh_manager.manager;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 分布式锁管理器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DistributedLockManager {
    private final RedissonClient redissonClient;
    private final String LOCK_KEY_PREFIX = "lock:";

    /**
     * 非阻塞方式执行带锁操作
     *
     * @param lockKey      锁的key
     * @param runSupplier  获取锁成功时执行的操作
     * @param elseSupplier 获取锁失败时执行的操作
     * @param <T>          返回值类型
     * @return 操作结果
     */
    public <T> T nonBlockExecute(String lockKey, Supplier<T> runSupplier, Supplier<T> elseSupplier) {

        //获取分布式锁
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + lockKey);
        //尝试获取锁，不等待
        if (lock.tryLock()) {
            try {
                // 获取锁成功，执行操作
                return runSupplier.get();
            } finally {
                // 释放锁
                lock.unlock();
            }
        } else {
            // 获取锁失败，执行备用操作
            return elseSupplier.get();
        }
    }
}
