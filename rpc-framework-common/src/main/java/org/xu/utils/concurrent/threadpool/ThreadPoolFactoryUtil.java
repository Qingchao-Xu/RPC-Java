package org.xu.utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建线程池的工具类
 */
@Slf4j
public class ThreadPoolFactoryUtil {
    /**
     * 通过 threadNamePrefix 来区分不同线程池（我们可以把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）。
     * key: threadNamePrefix
     * value: threadPool
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil() { // 工具类，私有构造方法

    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon));
        // 如果 threadPool 被 关闭 就重新创建一个
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadNamePrefix);
            threadPool = createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon);
            THREAD_POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix) {
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    private static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(), customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(), customThreadPoolConfig.getUnit(), customThreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    /**
     * 创建 ThreadFactory，如果 threadNamePrefix为空，则使用defaultThreadFactory
     * @param threadNamePrefix 线程名字前缀
     * @param daemon 是否为守护线程
     * @return ThreadFactory
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * 关闭所有线程池
     */
    public static void shutDownAllThreadPool() {
        log.info("call shutDownAllThreadPool method");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            // 先停止接收新任务，然后等待旧任务执行完，如果被打断，则立即停止所有的任务
            executorService.shutdown(); // 停止接收任务，原来的任务继续执行
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) { // 阻塞当前线程，知道线程池任务都执行完，或超时
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Thread pool never terminated");
                executorService.shutdownNow(); // 停止接收任务，原来的任务继续执行
            }
        });
    }

    /**
     * 打印线程池状态
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        try (ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false))) {
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                log.info("============ThreadPool Status=============");
                log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
                log.info("Active Threads: [{}]", threadPool.getActiveCount());
                log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
                log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
                log.info("===========================================");
            }, 0, 1, TimeUnit.SECONDS);
        }
    }
}