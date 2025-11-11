package server;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

/**
 * Feature 5: Load Balancing & Connection Pooling
 * Demonstrates: ThreadPoolExecutor, ExecutorService, Connection pooling, Resource management
 * Contributor: Member 5
 */
public class ConnectionPoolManager {
    // Core pool configuration
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 50;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 100;

    // Thread pool executor
    private static ThreadPoolExecutor executor;
    private static ScheduledExecutorService monitorExecutor;

    // Statistics
    private static AtomicInteger activeConnections = new AtomicInteger(0);
    private static AtomicInteger totalConnectionsServed = new AtomicInteger(0);
    private static AtomicInteger rejectedConnections = new AtomicInteger(0);
    private static long startTime;

    // Connection limits
    private static final int MAX_CONNECTIONS_PER_IP = 5;
    private static Map<String, Integer> connectionsPerIP = new ConcurrentHashMap<>();

    public static void initialize() {
        startTime = System.currentTimeMillis();

        // Create blocking queue for pending connections
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        // Create custom thread factory
        ThreadFactory threadFactory = new ThreadFactory() {
            private AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "AuctionWorker-" + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        };

        // Create custom rejection handler
        RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                rejectedConnections.incrementAndGet();
                System.err.println("Connection rejected! Queue is full. Total rejected: " + 
                                 rejectedConnections.get());
            }
        };

        // Initialize thread pool executor
        executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            workQueue,
            threadFactory,
            rejectionHandler
        );

        // Allow core threads to timeout
        executor.allowCoreThreadTimeOut(true);

        // Start monitoring service
        monitorExecutor = Executors.newScheduledThreadPool(1);
        monitorExecutor.scheduleAtFixedRate(() -> printStatistics(), 30, 30, TimeUnit.SECONDS);

        System.out.println("Connection Pool Manager initialized");
        System.out.println("Core pool size: " + CORE_POOL_SIZE);
        System.out.println("Max pool size: " + MAX_POOL_SIZE);
        System.out.println("Queue capacity: " + QUEUE_CAPACITY);
    }

    /**
     * Submit a connection task to the pool
     */
    public static boolean submitConnection(String clientIP, Runnable task) {
        // Check per-IP connection limit
        if (!checkConnectionLimit(clientIP)) {
            System.err.println("Connection limit exceeded for IP: " + clientIP);
            rejectedConnections.incrementAndGet();
            return false;
        }

        try {
            // Wrap task to track connection lifecycle
            Runnable wrappedTask = () -> {
                try {
                    activeConnections.incrementAndGet();
                    totalConnectionsServed.incrementAndGet();
                    incrementIPConnection(clientIP);
                    
                    task.run();
                    
                } finally {
                    activeConnections.decrementAndGet();
                    decrementIPConnection(clientIP);
                }
            };

            executor.execute(wrappedTask);
            return true;
        } catch (RejectedExecutionException e) {
            rejectedConnections.incrementAndGet();
            return false;
        }
    }

    /**
     * Check if client IP has exceeded connection limit
     */
    private static boolean checkConnectionLimit(String ip) {
        Integer count = connectionsPerIP.getOrDefault(ip, 0);
        return count < MAX_CONNECTIONS_PER_IP;
    }

    private static void incrementIPConnection(String ip) {
        connectionsPerIP.merge(ip, 1, Integer::sum);
    }

    private static void decrementIPConnection(String ip) {
        connectionsPerIP.computeIfPresent(ip, (k, v) -> v > 1 ? v - 1 : null);
    }

    /**
     * Get current pool statistics
     */
    public static PoolStatistics getStatistics() {
        return new PoolStatistics(
            executor.getActiveCount(),
            executor.getPoolSize(),
            executor.getQueue().size(),
            executor.getCompletedTaskCount(),
            activeConnections.get(),
            totalConnectionsServed.get(),
            rejectedConnections.get(),
            connectionsPerIP.size()
        );
    }

    /**
     * Print pool statistics
     */
    private static void printStatistics() {
        PoolStatistics stats = getStatistics();
        long uptime = (System.currentTimeMillis() - startTime) / 1000;
        
        System.out.println("\n=== Connection Pool Statistics ===");
        System.out.println("Uptime: " + uptime + " seconds");
        System.out.println("Active threads: " + stats.activeThreads);
        System.out.println("Pool size: " + stats.poolSize + "/" + MAX_POOL_SIZE);
        System.out.println("Queue size: " + stats.queueSize + "/" + QUEUE_CAPACITY);
        System.out.println("Active connections: " + stats.activeConnections);
        System.out.println("Total served: " + stats.totalConnectionsServed);
        System.out.println("Rejected: " + stats.rejectedConnections);
        System.out.println("Unique IPs: " + stats.uniqueIPs);
        System.out.println("Completed tasks: " + stats.completedTasks);
        
        if (uptime > 0) {
            System.out.println("Avg connections/sec: " + (stats.totalConnectionsServed / uptime));
        }
        
        System.out.println("===================================\n");
    }

    /**
     * Graceful shutdown
     */
    public static void shutdown() {
        System.out.println("Shutting down Connection Pool Manager...");
        
        executor.shutdown();
        monitorExecutor.shutdown();
        
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!monitorExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                monitorExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            monitorExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        printStatistics();
        System.out.println("Connection Pool Manager shutdown complete");
    }

    /**
     * Check pool health
     */
    public static boolean isHealthy() {
        if (executor.isShutdown() || executor.isTerminated()) {
            return false;
        }
        
        int queueSize = executor.getQueue().size();
        int poolSize = executor.getPoolSize();
        
        // Pool is unhealthy if queue is nearly full and pool is at max
        return !(queueSize > QUEUE_CAPACITY * 0.9 && poolSize >= MAX_POOL_SIZE);
    }

    /**
     * Get pool health status
     */
    public static String getHealthStatus() {
        if (!isHealthy()) {
            return "CRITICAL - Pool overloaded";
        }
        
        double queueUsage = (double) executor.getQueue().size() / QUEUE_CAPACITY;
        double poolUsage = (double) executor.getPoolSize() / MAX_POOL_SIZE;
        
        if (queueUsage > 0.7 || poolUsage > 0.8) {
            return "WARNING - High load";
        }
        
        return "HEALTHY";
    }
}

/**
 * Pool statistics data class
 */
class PoolStatistics {
    public final int activeThreads;
    public final int poolSize;
    public final int queueSize;
    public final long completedTasks;
    public final int activeConnections;
    public final int totalConnectionsServed;
    public final int rejectedConnections;
    public final int uniqueIPs;

    public PoolStatistics(int activeThreads, int poolSize, int queueSize, long completedTasks,
                         int activeConnections, int totalConnectionsServed, 
                         int rejectedConnections, int uniqueIPs) {
        this.activeThreads = activeThreads;
        this.poolSize = poolSize;
        this.queueSize = queueSize;
        this.completedTasks = completedTasks;
        this.activeConnections = activeConnections;
        this.totalConnectionsServed = totalConnectionsServed;
        this.rejectedConnections = rejectedConnections;
        this.uniqueIPs = uniqueIPs;
    }

    @Override
    public String toString() {
        return String.format("PoolStats[active=%d, pool=%d, queue=%d, completed=%d, rejected=%d]",
                           activeThreads, poolSize, queueSize, completedTasks, rejectedConnections);
    }
}
