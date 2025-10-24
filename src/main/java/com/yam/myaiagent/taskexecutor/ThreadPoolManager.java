package com.yam.myaiagent.taskexecutor;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池管理工具类，拒绝策略为：由当前线程执行（CallerRunsPolicy）
 */
public class ThreadPoolManager {
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final int QUEUE_CAPACITY = 100;
    private static final long KEEP_ALIVE_TIME = 60L;

    /**
     * -- GETTER --
     *  获取全局线程池
     */
    @Getter
    private static final ThreadPoolExecutor executor;

    static {
        executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                new NamedThreadFactory("TaskExecutorPool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 自定义线程工厂，便于排查问题
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final String baseName;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        NamedThreadFactory(String baseName) {
            this.baseName = baseName;
        }

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread t = new Thread(r, baseName + threadNumber.getAndIncrement());
            t.setDaemon(false);
            return t;
        }
    }
}