package net.shuyanmc.mpem.async.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.async.AsyncSystemInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@AsyncHandler(threadPool = "compute", fallbackToSync = true)
public class StructureGenAsync {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BlockingQueue<StructureTask> structureQueue = new LinkedBlockingQueue<>(1000);
    private static final int MAX_RETRIES = 5;
    private static final int MAX_TASKS_PER_TICK = 50;
    private static final int CHUNK_LOAD_RADIUS = 2;
    private static final Semaphore taskSemaphore = new Semaphore(200);
    private static final long TASK_TIMEOUT_MS = 30000;

    public static void init() {
        LOGGER.info("Async Structure Generator initialized");
    }

    public static void shutdown() {
        structureQueue.clear();
        LOGGER.info("Async Structure Generator shutdown completed");
    }

    public static CompletableFuture<Void> placeStructureAsync(ServerLevel level, StructureTemplate template, BlockPos pos) {
        if (level == null || template == null || pos == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid parameters"));
        }

        if (!level.isLoaded(pos)) {
            LOGGER.warn("Attempted to place structure at unloaded position {}", pos);
            return CompletableFuture.failedFuture(new IllegalStateException("Position unloaded"));
        }

        if (!taskSemaphore.tryAcquire()) {
            LOGGER.warn("Too many concurrent structure generation tasks, skipping {}", pos);
            return CompletableFuture.failedFuture(new IllegalStateException("Too many concurrent tasks"));
        }

        StructureTask task = new StructureTask(level, template, pos);
        if (!structureQueue.offer(task)) {
            taskSemaphore.release();
            LOGGER.warn("Structure queue full, skipping generation at {}", pos);
            return CompletableFuture.failedFuture(new IllegalStateException("Queue full"));
        }

        return task.future().whenComplete((r, e) -> taskSemaphore.release());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            processStructureTasks();
        }
    }

    private static void processStructureTasks() {
        int processed = 0;
        while (processed < MAX_TASKS_PER_TICK && !structureQueue.isEmpty()) {
            StructureTask task = structureQueue.poll();
            if (task != null) {
                processed++;
                processSingleTask(task);
            }
        }
    }

    private static void processSingleTask(StructureTask task) {
        AsyncSystemInitializer.getThreadPool("compute").execute(() -> {
            try {
                if (System.currentTimeMillis() - task.creationTime() > TASK_TIMEOUT_MS) {
                    LOGGER.error("Structure generation timed out at {}", task.pos());
                    task.future().completeExceptionally(new TimeoutException("Operation timed out"));
                    return;
                }

                if (task.retryCount().incrementAndGet() > MAX_RETRIES) {
                    LOGGER.error("Structure generation failed after {} retries at {}", MAX_RETRIES, task.pos());
                    task.future().completeExceptionally(new TimeoutException("Max retries exceeded"));
                    return;
                }

                if (!isAreaLoaded(task.level(), task.pos())) {
                    LOGGER.debug("Area unloaded during generation at {}, retrying...", task.pos());
                    retryTask(task);
                    return;
                }

                if (!isTerrainReady(task.level(), task.pos())) {
                    LOGGER.debug("Terrain not ready for structure at {}, retrying...", task.pos());
                    retryTask(task);
                    return;
                }

                placeStructureSafely(task);
                task.future().complete(null);
            } catch (Exception e) {
                LOGGER.error("Failed to place structure at {}", task.pos(), e);
                task.future().completeExceptionally(e);
            }
        });
    }

    private static boolean isAreaLoaded(ServerLevel level, BlockPos pos) {
        try {
            return level.isAreaLoaded(pos, CHUNK_LOAD_RADIUS);
        } catch (Exception e) {
            LOGGER.warn("Failed to check area load status at {}", pos, e);
            return false;
        }
    }

    private static boolean isTerrainReady(ServerLevel level, BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        try {
            return level.getChunkSource()
                    .getChunk(chunkX, chunkZ, ChunkStatus.FEATURES, false)
                    .getStatus()
                    .isOrAfter(ChunkStatus.FEATURES);
        } catch (Exception e) {
            LOGGER.warn("Failed to check terrain status at [{}, {}]", chunkX, chunkZ, e);
            return false;
        }
    }

    private static void placeStructureSafely(StructureTask task) {
        try {
            task.template().placeInWorld(
                    task.level(),
                    task.pos(),
                    task.pos(),
                    new StructurePlaceSettings(),
                    task.level().random,
                    2
            );
            LOGGER.debug("Successfully placed structure at {}", task.pos());
        } catch (Exception e) {
            throw new RuntimeException("Failed to place structure", e);
        }
    }

    private static void retryTask(StructureTask task) {
        if (!structureQueue.offer(task)) {
            LOGGER.warn("Failed to retry structure generation at {}, queue full", task.pos());
            task.future().completeExceptionally(new IllegalStateException("Queue full during retry"));
        }
    }

    private static class StructureTask {
        private final ServerLevel level;
        private final StructureTemplate template;
        private final BlockPos pos;
        private final AtomicInteger retryCount = new AtomicInteger(0);
        private final long creationTime = System.currentTimeMillis();
        private final CompletableFuture<Void> future = new CompletableFuture<>();

        public StructureTask(ServerLevel level, StructureTemplate template, BlockPos pos) {
            this.level = level;
            this.template = template;
            this.pos = pos;
        }

        public ServerLevel level() { return level; }
        public StructureTemplate template() { return template; }
        public BlockPos pos() { return pos; }
        public AtomicInteger retryCount() { return retryCount; }
        public long creationTime() { return creationTime; }
        public CompletableFuture<Void> future() { return future; }
    }
}