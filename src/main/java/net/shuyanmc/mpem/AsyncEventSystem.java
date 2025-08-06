package net.shuyanmc.mpem;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.shuyanmc.mpem.config.CoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class AsyncEventSystem {
    public static final Logger LOGGER = LogManager.getLogger("MPEM-Async");
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.min(4, Math.max(CoolConfig.maxCPUPro.get(), CPU_CORES));
    private static final int MAX_POOL_SIZE = Math.min(16, CPU_CORES * 2);
    private static final long KEEP_ALIVE_TIME = 30L;
    private static final int WORK_QUEUE_SIZE = 2000;

    private static final ThreadPoolExecutor ASYNC_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(WORK_QUEUE_SIZE),
            new AsyncEventThreadFactory(),
            new AsyncEventRejectedHandler()
    ) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            adjustPoolSize();
        }
    };

    private static final ConcurrentHashMap<Class<? extends Event>, EventTypeInfo> EVENT_TYPE_INFOS = new ConcurrentHashMap<>(64);
    private static volatile boolean initialized = false;
    private static final AtomicLong totalAsyncTasks = new AtomicLong(0);
    private static final AtomicLong failedAsyncTasks = new AtomicLong(0);
    private static final AtomicLong lastAdjustTime = new AtomicLong(System.currentTimeMillis());

    static {
        ASYNC_EXECUTOR.prestartAllCoreThreads();
    }

    private static void adjustPoolSize() {
        long now = System.currentTimeMillis();
        if (now - lastAdjustTime.get() < 30000) {
            return;
        }

        int activeCount = ASYNC_EXECUTOR.getActiveCount();
        int queueSize = ASYNC_EXECUTOR.getQueue().size();
        int newCoreSize = Math.min(MAX_POOL_SIZE, Math.max(2, activeCount + (queueSize / 100)));

        ASYNC_EXECUTOR.setCorePoolSize(newCoreSize);
        lastAdjustTime.set(now);
    }

    private static class EventTypeInfo {
        volatile boolean async;
        volatile boolean healthy = true;
        final AtomicInteger pendingTasks = new AtomicInteger(0);
        final AtomicInteger failedCount = new AtomicInteger(0);
        volatile boolean isClientEvent = false;
        volatile long lastFailureTime = 0;

        EventTypeInfo(boolean async) {
            this.async = async;
        }

        boolean shouldRetryAsync() {
            return async && healthy && (failedCount.get() < 3 ||
                    System.currentTimeMillis() - lastFailureTime > 60000);
        }
    }

    private static class AsyncEventThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "MPEM-Async-Worker-" + counter.incrementAndGet());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.setUncaughtExceptionHandler((t, e) -> {
                LOGGER.error("Uncaught exception in async thread", e);
            });
            return thread;
        }
    }

    private static class AsyncEventRejectedHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            LOGGER.warn("Queue full ({} tasks), executing on caller thread", executor.getQueue().size());
            if (!executor.isShutdown()) {
                r.run();
            }
        }
    }

    public static void initialize() {
        if (!CoolConfig.isEnabled() || initialized) {
            return;
        }

        registerCommonAsyncEvents();
        initialized = true;
        LOGGER.info("Initialized with core: {}, max: {}, queue: {}",
                CORE_POOL_SIZE, MAX_POOL_SIZE, WORK_QUEUE_SIZE);
    }

    private static void registerCommonAsyncEvents() {
        if (!CoolConfig.isEnabled()) {
            return;
        }

        String[] asyncEvents = {
                "net.minecraftforge.event.entity.player.PlayerEvent",
                "net.minecraftforge.event.entity.player.AdvancementEvent",
                "net.minecraftforge.event.entity.player.AnvilRepairEvent",
                "net.minecraftforge.event.entity.player.PlayerInteractEvent",
                "net.minecraftforge.event.entity.player.PlayerXpEvent",
                "net.minecraftforge.event.level.BlockEvent",
                "net.minecraftforge.event.level.ChunkEvent",
                "net.minecraftforge.event.level.ExplosionEvent",
                "net.minecraftforge.event.entity.EntityEvent",
                "net.minecraftforge.event.entity.EntityJoinLevelEvent",
                "net.minecraftforge.event.entity.EntityLeaveLevelEvent",
                "net.minecraftforge.event.entity.EntityMountEvent",
                "net.minecraftforge.event.entity.EntityTeleportEvent",
                "net.minecraftforge.event.entity.item.ItemEvent",
                "net.minecraftforge.event.entity.item.ItemExpireEvent",
                "net.minecraftforge.event.entity.item.ItemTossEvent",
                "net.minecraftforge.event.level.LevelEvent",
                "net.minecraftforge.event.level.BlockEvent",
                "net.minecraftforge.event.level.ChunkEvent",
                "net.minecraftforge.event.network.CustomPayloadEvent",
                "net.minecraftforge.event.CommandEvent",
                "net.minecraftforge.event.TagsUpdatedEvent",
                "net.minecraftforge.event.LootTableLoadEvent",
                "net.minecraftforge.event.RegisterCommandsEvent"
        };

        String[] syncEvents = {
                "net.minecraftforge.event.TickEvent",
                "net.minecraftforge.event.level.LevelTickEvent",
                "net.minecraftforge.event.entity.living.LivingEvent",
                "net.minecraftforge.event.entity.living.LivingAttackEvent",
                "net.minecraftforge.event.entity.living.LivingDamageEvent",
                "net.minecraftforge.event.entity.living.LivingDeathEvent",
                "net.minecraftforge.event.entity.living.LivingDropsEvent",
                "net.minecraftforge.event.entity.living.LivingExperienceDropEvent",
                "net.minecraftforge.event.entity.living.LivingHealEvent",
                "net.minecraftforge.event.entity.living.LivingKnockBackEvent",
                "net.minecraftforge.event.server.ServerStartingEvent",
                "net.minecraftforge.event.server.ServerStoppingEvent",
                "net.minecraftforge.event.server.ServerStartedEvent"
        };

        for (String className : asyncEvents) {
            try {
                Class<? extends Event> eventClass = loadClass(className);
                if (isClientOnlyEvent(eventClass)) {
                    LOGGER.debug("Skipping client event: {}", className);
                    continue;
                }
                registerAsyncEvent(eventClass);
            } catch (ClassNotFoundException e) {
                LOGGER.warn("[Fallback] Failed to load async event: {}, falling back to SYNC", className);
                try {
                    Class<? extends Event> eventClass = loadClass(className);
                    registerSyncEvent(eventClass);
                } catch (ClassNotFoundException ex) {
                    LOGGER.error("[Critical] Event class not found: {}", className);
                }
            }
        }

        for (String className : syncEvents) {
            try {
                Class<? extends Event> eventClass = loadClass(className);
                registerSyncEvent(eventClass);
            } catch (ClassNotFoundException e) {
                LOGGER.error("[Critical] Sync event class not found: {}", className);
            }
        }

        LOGGER.info("Registered {} async event types", EVENT_TYPE_INFOS.size());
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Event> loadClass(String className) throws ClassNotFoundException {
        try {
            Class<?> clazz = Class.forName(className);
            if (!Event.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class " + className + " does not extend Event");
            }
            return (Class<? extends Event>) clazz;
        } catch (ClassNotFoundException e) {
            ClassLoader forgeLoader = Event.class.getClassLoader();
            Class<?> clazz = Class.forName(className, true, forgeLoader);
            return (Class<? extends Event>) clazz;
        }
    }

    public static boolean isClientOnlyEvent(Class<? extends Event> eventClass) {
        return eventClass.getName().startsWith("client");
    }

    public static void registerAsyncEvent(Class<? extends Event> eventType) {
        if (!CoolConfig.isEnabled()) return;
        EVENT_TYPE_INFOS.compute(eventType, (k, v) -> {
            if (v == null) {
                EventTypeInfo info = new EventTypeInfo(true);
                info.isClientEvent = isClientOnlyEvent(eventType);
                return info;
            }
            v.async = true;
            v.healthy = true;
            v.failedCount.set(0);
            v.isClientEvent = isClientOnlyEvent(eventType);
            return v;
        });
        LOGGER.debug("Registered async event: {}", eventType.getName());
    }

    public static void registerSyncEvent(Class<? extends Event> eventType) {
        if (!CoolConfig.isEnabled()) return;
        EVENT_TYPE_INFOS.compute(eventType, (k, v) -> {
            if (v == null) {
                EventTypeInfo info = new EventTypeInfo(false);
                info.isClientEvent = isClientOnlyEvent(eventType);
                return info;
            }
            v.async = false;
            v.isClientEvent = isClientOnlyEvent(eventType);
            return v;
        });
        LOGGER.debug("Registered sync event: {}", eventType.getName());
    }

    public static boolean shouldHandleAsync(Class<? extends Event> eventType) {
        EventTypeInfo info = EVENT_TYPE_INFOS.get(eventType);
        if (info != null) {
            if (info.isClientEvent && FMLEnvironment.dist.isDedicatedServer()) {
                return false;
            }
            return info.async && info.healthy;
        }
        return eventType.getSimpleName().contains("Async");
    }

    public static CompletableFuture<Void> executeAsync(Class<? extends Event> eventType, Runnable task) {
        if (!CoolConfig.isEnabled()) {
            task.run();
            return CompletableFuture.completedFuture(null);
        }

        totalAsyncTasks.incrementAndGet();

        if (shouldExecuteImmediately(eventType)) {
            task.run();
            return CompletableFuture.completedFuture(null);
        }

        EventTypeInfo info = getEventTypeInfo(eventType);
        if (!info.async || !info.healthy) {
            task.run();
            return CompletableFuture.completedFuture(null);
        }

        info.pendingTasks.incrementAndGet();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

        return CompletableFuture.runAsync(() -> {
            Thread.currentThread().setContextClassLoader(contextLoader);
            try {
                executeTask(eventType, task, info);
            } catch (Throwable t) {
                handleTaskFailure(eventType, info, t);
                throw t;
            } finally {
                info.pendingTasks.decrementAndGet();
            }
        }, ASYNC_EXECUTOR).exceptionally(ex -> {
            LOGGER.warn("Retrying {} synchronously", eventType.getSimpleName());
            task.run();
            return null;
        });
    }
    private static boolean shouldExecuteImmediately(Class<? extends Event> eventType) {
        return eventType.getName().contains("Client") && FMLEnvironment.dist.isDedicatedServer() ||
                !initialized;
    }

    private static EventTypeInfo getEventTypeInfo(Class<? extends Event> eventType) {
        return EVENT_TYPE_INFOS.computeIfAbsent(
                eventType,
                k -> new EventTypeInfo(shouldHandleAsync(eventType))
        );
    }

    private static void executeTask(Class<? extends Event> eventType, Runnable task, EventTypeInfo info) {
        long startTime = System.nanoTime();
        task.run();
        long elapsed = System.nanoTime() - startTime;

        if (elapsed > TimeUnit.MILLISECONDS.toNanos(100)) {
            LOGGER.debug("Slow task {}: {}ms",
                    eventType.getSimpleName(),
                    TimeUnit.NANOSECONDS.toMillis(elapsed));
        }
    }

    private static void handleTaskFailure(Class<? extends Event> eventType, EventTypeInfo info, Throwable t) {
        failedAsyncTasks.incrementAndGet();
        info.failedCount.incrementAndGet();
        info.lastFailureTime = System.currentTimeMillis();

        LOGGER.error("Task failed: {}", eventType.getSimpleName(), t);

        if (CoolConfig.DISABLE_ASYNC_ON_ERROR.get() || info.failedCount.get() >= 3) {
            info.healthy = false;
            LOGGER.warn("Disabled async for {}", eventType.getSimpleName());
        }
    }
    public static void shutdown() {
        if (!CoolConfig.isEnabled()) return;
        if (!initialized) return;

        LOGGER.info("Shutting down async event system. Total tasks: {}, Failed: {}",
                totalAsyncTasks.get(), failedAsyncTasks.get());

        ASYNC_EXECUTOR.shutdown();
        try {
            if (!ASYNC_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                LOGGER.warn("Forcing async event executor shutdown");
                ASYNC_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            ASYNC_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static int getQueueSize() {
        return ASYNC_EXECUTOR.getQueue().size();
    }

    public static int getActiveCount() {
        return ASYNC_EXECUTOR.getActiveCount();
    }

    public static int getPoolSize() {
        return ASYNC_EXECUTOR.getPoolSize();
    }

    public static int getMaxPoolSize() {
        return MAX_POOL_SIZE;
    }

    public static int getAsyncEventCount() {
        return EVENT_TYPE_INFOS.size();
    }

    public static void tryRegisterAsyncEvent(Consumer<?> consumer) {
        try {
            for (Type type : consumer.getClass().getGenericInterfaces()) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) type;
                    if (paramType.getRawType().equals(Consumer.class)) {
                        Type[] typeArgs = paramType.getActualTypeArguments();
                        if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                            Class<?> eventClass = (Class<?>) typeArgs[0];
                            if (Event.class.isAssignableFrom(eventClass)) {
                                @SuppressWarnings("unchecked")
                                Class<? extends Event> eventType = (Class<? extends Event>) eventClass;
                                registerAsyncEvent(eventType);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to determine event type for consumer: {}", consumer.getClass().getName(), e);
        }
    }

    public static void resetEventTypeHealth(Class<? extends Event> eventType) {
        EVENT_TYPE_INFOS.computeIfPresent(eventType, (k, v) -> {
            v.healthy = true;
            v.failedCount.set(0);
            return v;
        });
    }

}