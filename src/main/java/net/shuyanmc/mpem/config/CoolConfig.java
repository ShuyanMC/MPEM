//此部分代码使用AI优化格式
package net.shuyanmc.mpem.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.*;
import java.util.function.Consumer;

public class CoolConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;
    private static Consumer<Void> changeListener;

    // ==================== 渲染优化 | Rendering Optimization ====================
    public static final ForgeConfigSpec.BooleanValue FORCE_METAL;
    public static final ForgeConfigSpec.BooleanValue ENABLEDCULL;
    public static final ForgeConfigSpec.IntValue CULLING_DEPTH;
    public static final ForgeConfigSpec.DoubleValue REJECTION_RATE;
    public static final ForgeConfigSpec.BooleanValue ULTRA_CULLING;
    public static final ForgeConfigSpec.IntValue ULTRA_DEPTH;
    public static final ForgeConfigSpec.DoubleValue ULTRA_BACKFACE;
    public static final ForgeConfigSpec.BooleanValue ADVANCED_CULLING;
    public static final ForgeConfigSpec.BooleanValue REDUCE_FPS_WHEN_INACTIVE;
    public static final ForgeConfigSpec.IntValue INACTIVE_FPS_LIMIT;
    public static final ForgeConfigSpec.BooleanValue REDUCE_RENDER_DISTANCE_WHEN_INACTIVE;
    public static final ForgeConfigSpec.IntValue INACTIVE_RENDER_DISTANCE;
    public static ForgeConfigSpec.BooleanValue useAsyncTracing;
    public static ForgeConfigSpec.IntValue tracingThreads;
    public static ForgeConfigSpec.DoubleValue traceDistance;
    public static ForgeConfigSpec.DoubleValue fallbackDistance;
    public static ForgeConfigSpec.BooleanValue useAdvancedLeafCulling;
    public static ForgeConfigSpec.IntValue minLeafConnections;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_MANGROVE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_OPTIMIZATION;
    public static final ForgeConfigSpec.EnumValue<RenderMode> RENDER_MODE;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_ENDER_CHESTS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_TRAPPED_CHESTS;
    public static final ForgeConfigSpec.IntValue MAX_RENDER_DISTANCE;
    // ==================== 实体优化 | Entity Optimization ====================
    public static ForgeConfigSpec.BooleanValue disableEntityCollisions;
    public static ForgeConfigSpec.BooleanValue optimizeEntityAI;
    public static ForgeConfigSpec.IntValue entityActivationRange;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_ENTITY_CLEANUP;
    public static final ForgeConfigSpec.BooleanValue reduceEntityUpdates;
    public static ForgeConfigSpec.BooleanValue optimizeEntities;
    public static ForgeConfigSpec.IntValue horizontalRange;
    public static ForgeConfigSpec.IntValue verticalRange;
    public static ForgeConfigSpec.BooleanValue ignoreDeadEntities;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> entityWhitelist;
    public static ForgeConfigSpec.BooleanValue tickRaidersInRaid;

    // ==================== 物品优化 | Item Optimization ====================
    public static ForgeConfigSpec.BooleanValue OpenIO;
    public static ForgeConfigSpec.IntValue maxStackSize;
    public static ForgeConfigSpec.DoubleValue mergeDistance;
    public static ForgeConfigSpec.IntValue listMode;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> itemList;
    public static ForgeConfigSpec.BooleanValue showStackCount;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue MAX_STACK_SIZE;
    public static ForgeConfigSpec.BooleanValue optimizeItems;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> itemWhitelist;

    // ==================== 内存优化 | Memory Optimization ====================
    public static final ForgeConfigSpec.IntValue MEMORY_CLEAN_INTERVAL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GC;

    // ==================== 渐进加载 | Progressive Loading ====================
    public static final ForgeConfigSpec.IntValue ENTITY_LOAD_BATCH_SIZE;
    public static final ForgeConfigSpec.IntValue TILE_ENTITY_LOAD_BATCH_SIZE;
    public static final ForgeConfigSpec.IntValue MIN_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue MAX_TICK_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue TPS_ADJUST_THRESHOLD;

    // ==================== 区块优化 | Chunk Optimization ====================
    public static ForgeConfigSpec.BooleanValue aggressiveChunkUnloading;
    public static ForgeConfigSpec.IntValue chunkUnloadDelay;
    public static final ForgeConfigSpec.BooleanValue reduceChunkUpdates;
    public static final ForgeConfigSpec.IntValue CHUNK_GEN_THREADS;

    // ==================== 异步优化 | Async Optimization ====================
    public static final ForgeConfigSpec.BooleanValue ASYNC_PARTICLES;
    public static final ForgeConfigSpec.IntValue MAX_ASYNC_OPERATIONS_PER_TICK;
    public static final ForgeConfigSpec.BooleanValue DISABLE_ASYNC_ON_ERROR;
    public static final ForgeConfigSpec.IntValue ASYNC_EVENT_TIMEOUT;
    public static final ForgeConfigSpec.BooleanValue WAIT_FOR_ASYNC_EVENTS;
    public static ForgeConfigSpec.IntValue maxCPUPro;
    public static ForgeConfigSpec.IntValue maxthreads;

    // ==================== 事件系统 | Event System ====================
    public static ForgeConfigSpec.BooleanValue FEATURE_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ASYNC_EVENT_CLASS_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ASYNC_EVENT_MOD_BLACKLIST;
    public static final ForgeConfigSpec.BooleanValue STRICT_CLASS_CHECKING;

    // ==================== 调试选项 | Debug Options ====================
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING;
    public static final ForgeConfigSpec.BooleanValue LOG_BLOCK_EVENTS;

    // ==================== 其他设置 | Miscellaneous Settings ====================
    public static ForgeConfigSpec.BooleanValue sendLoginMessage;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> dimensionWhitelist;

    static {
        // ==================== 渲染优化设置 | Rendering Optimization Settings ====================
        BUILDER.push("渲染优化 | Rendering Optimization");

        // Metal 渲染器设置
        BUILDER.push("Metal 渲染器 | Metal Renderer");
        FORCE_METAL = BUILDER.comment(
                        "强制使用Metal渲染器（即使在非Mac系统上）",
                        "Force Metal renderer even on non-Mac systems")
                .define("forceMetal", false);
        BUILDER.pop();
        BUILDER.push("chest_optimization");

        ENABLE_OPTIMIZATION = BUILDER
                .comment("Enable chest rendering optimization")
                .define("enableOptimization", true);

        RENDER_MODE = BUILDER
                .comment("Rendering mode")
                .defineEnum("renderMode", RenderMode.SIMPLE);

        OPTIMIZE_ENDER_CHESTS = BUILDER
                .comment("Optimize ender chests")
                .define("optimizeEnderChests", true);

        OPTIMIZE_TRAPPED_CHESTS = BUILDER
                .comment("Optimize trapped chests")
                .define("optimizeTrappedChests", false);

        MAX_RENDER_DISTANCE = BUILDER
                .comment("Max render distance in chunks")
                .defineInRange("maxRenderDistance", 32, 1, 128);

        BUILDER.pop();

        // 剔除设置
        BUILDER.push("高级剔除 | Advanced Culling");
        ENABLEDCULL = BUILDER.comment(
                        "启用树叶渲染优化",
                        "Enable leaf rendering optimizations")
                .define("enabled", true);
        CULLING_DEPTH = BUILDER.comment(
                        "剔除深度 (1-5)，值越高性能越好但可能导致视觉异常",
                        "Culling depth (1-5), Higher values = better performance but may cause visual artifacts")
                .defineInRange("cullingDepth", 5, 1, 5);
        REJECTION_RATE = BUILDER.comment(
                        "随机剔除率 (0.0-1.0)，防止可见的剔除模式",
                        "Random rejection rate (0.0-1.0), Prevents visible culling patterns")
                .defineInRange("rejectionRate", 0.65, 0.0, 1.0);

        // 超激进剔除
        BUILDER.push("超激进剔除 | Ultra Culling");
        ULTRA_CULLING = BUILDER.comment(
                        "启用超激进剔除（对所有方块/实体生效）",
                        "Enable ultra-aggressive culling (affects all blocks/entities)")
                .define("ultraCulling", true);
        ULTRA_DEPTH = BUILDER.comment(
                        "剔除深度 (1-4)，值越大剔除越激进但可能出现bug",
                        "Culling depth (1-4), Higher values = more aggressive culling but may cause bugs")
                .defineInRange("ultraDepth", 5, 1, 4);
        ULTRA_BACKFACE = BUILDER.comment(
                        "仅剔除背向玩家的面（防止正面消失），值范围0.0-1.0",
                        "Only cull backfaces (prevent front-face disappearance), value range 0.0-1.0")
                .defineInRange("ultraBackfaceOnly", 0.95, 0.0, 1.0);  // 使用 defineInRange 并指定范围
        BUILDER.pop(); // 超激进剔除

        ADVANCED_CULLING = BUILDER.comment(
                        "使用高级剔除算法，更精确但稍慢",
                        "Use advanced culling algorithm, More precise but slightly slower")
                .define("advancedCulling", true);
        BUILDER.pop(); // 高级剔除

        // 异步路径追踪
        BUILDER.push("路径追踪 | Path Tracing");
        useAsyncTracing = BUILDER.comment(
                        "启用异步路径追踪进行剔除",
                        "Enable async path tracing for culling")
                .define("asyncTracing", true);
        tracingThreads = BUILDER.comment(
                        "路径追踪线程数 (1-8)",
                        "Number of threads for path tracing (1-8)")
                .defineInRange("tracingThreads", 4, 1, 8);
        traceDistance = BUILDER.comment(
                        "最大追踪距离（方块）",
                        "Max tracing distance in blocks")
                .defineInRange("traceDistance", 6.0, 1.0, 16.0);
        fallbackDistance = BUILDER.comment(
                        "回退简单剔除距离（方块）",
                        "Fallback simple culling distance in blocks")
                .defineInRange("fallbackDistance", 16.0, 4.0, 32.0);
        BUILDER.pop(); // 路径追踪

        // 树叶优化
        BUILDER.push("树叶优化 | Leaf Optimization");
        useAdvancedLeafCulling = BUILDER.comment(
                        "使用高级树叶剔除算法",
                        "Use advanced leaf culling algorithm")
                .define("advancedLeafCulling", true);
        minLeafConnections = BUILDER.comment(
                        "简单剔除所需的最小树叶连接数 (1-6)",
                        "Minimum connected leaves for simple culling (1-6)")
                .defineInRange("minConnections", 2, 1, 6);
        OPTIMIZE_MANGROVE = BUILDER.comment(
                        "启用红树林根优化",
                        "Enable mangrove roots optimization")
                .define("optimizeMangrove", true);
        BUILDER.pop(); // 树叶优化

        // 非活动状态优化
        BUILDER.push("非活动状态优化 | Inactive Optimization");
        REDUCE_FPS_WHEN_INACTIVE = BUILDER.comment(
                        "窗口非活动时降低FPS",
                        "Enable FPS reduction when window is inactive")
                .define("reduceFpsWhenInactive", false);
        INACTIVE_FPS_LIMIT = BUILDER.comment(
                        "非活动状态FPS限制 (5-60)",
                        "FPS limit when window is inactive (5-60)")
                .defineInRange("inactiveFpsLimit", 10, 5, 60);
        REDUCE_RENDER_DISTANCE_WHEN_INACTIVE = BUILDER.comment(
                        "窗口非活动时降低渲染距离",
                        "Enable render distance reduction when window is inactive")
                .define("reduceRenderDistanceWhenInactive", false);
        INACTIVE_RENDER_DISTANCE = BUILDER.comment(
                        "非活动状态渲染距离 (2-12)",
                        "Render distance when window is inactive (2-12)")
                .defineInRange("inactiveRenderDistance", 2, 2, 12);
        BUILDER.pop(); // 非活动状态优化

        BUILDER.pop(); // 渲染优化

        // ==================== 实体优化设置 | Entity Optimization Settings ====================
        BUILDER.comment("实体优化 | Entity Optimization").push("entity_optimization");

        disableEntityCollisions = BUILDER.comment(
                        "优化实体碰撞检测",
                        "Optimize entity collision detection")
                .define("disableEntityCollisions", true);

        optimizeEntityAI = BUILDER.comment(
                        "优化实体AI计算",
                        "Optimize entity AI calculations")
                .define("optimizeEntityAI", true);

        entityActivationRange = BUILDER.comment(
                        "实体激活范围 (方块)",
                        "Entity activation range (blocks)")
                .defineInRange("entityActivationRange", 32, 16, 128);

        OPTIMIZE_ENTITY_CLEANUP = BUILDER.comment(
                        "启用死亡实体清理",
                        "Enable dead entity cleanup")
                .define("entityCleanup", true);

        reduceEntityUpdates = BUILDER.comment(
                        "减少远处实体的同步频率",
                        "Reduce entity sync frequency for distant entities")
                .define("reduceEntityUpdates", true);

        BUILDER.push("实体Tick优化 | Entity Tick Optimization");
        optimizeEntities = BUILDER.comment(
                        "启用实体tick优化",
                        "Enable entity tick optimization")
                .define("optimizeEntities", true);
        horizontalRange = BUILDER.comment(
                        "水平检测范围(方块)",
                        "Horizontal detection range (blocks)")
                .defineInRange("horizontalRange", 64, 1, 256);
        verticalRange = BUILDER.comment(
                        "垂直检测范围(方块)",
                        "Vertical detection range (blocks)")
                .defineInRange("verticalRange", 32, 1, 256);
        ignoreDeadEntities = BUILDER.comment(
                        "忽略已死亡的实体",
                        "Ignore dead entities")
                .define("ignoreDeadEntities", false);
        BUILDER.pop(); // 实体Tick优化

        BUILDER.push("实体白名单 | Entity Whitelist");
        entityWhitelist = BUILDER.comment(
                        "实体白名单（始终不优化）",
                        "Entity whitelist (always optimized)")
                .defineList("entityWhitelist", List.of("minecraft:ender_dragon"), o -> true);
        BUILDER.pop(); // 实体白名单

        BUILDER.push("袭击事件 | Raid Events");
        tickRaidersInRaid = BUILDER.comment(
                        "在袭击中保持袭击者tick",
                        "Keep raider ticking during raids")
                .define("tickRaidersInRaid", true);
        BUILDER.pop(); // 袭击事件

        BUILDER.pop(); // 实体优化

        // ==================== 物品优化设置 | Item Optimization Settings ====================
        BUILDER.comment("物品优化 | Item Optimization").push("item_optimization");

        OpenIO = BUILDER.comment(
                        "启用物品优化系统",
                        "Enable item optimization system")
                .define("OpenIO", true);

        BUILDER.push("堆叠合并 | Stack Merging");
        maxStackSize = BUILDER.comment(
                        "合并物品的最大堆叠数量（-1表示无限制）",
                        "Maximum stack size for merged items (-1 = no limit)")
                .defineInRange("maxStackSize", -1, -1, Integer.MAX_VALUE);
        mergeDistance = BUILDER.comment(
                        "物品合并检测半径（方块）",
                        "Item merge detection radius in blocks")
                .defineInRange("mergeDistance", 1.5, 0.1, 10.0);
        showStackCount = BUILDER.comment(
                        "在合并后的物品上显示堆叠数量",
                        "Show stack count on merged items")
                .define("showStackCount", true);
        BUILDER.pop(); // 堆叠合并

        BUILDER.push("自定义堆叠 | Custom Stack Size");
        ENABLED = BUILDER.comment(
                        "启用自定义堆叠大小-这里改了出问题的改回去，记住这句话！特别是科技服腐竹！",
                        "Enable custom stack sizes")
                .define("enabled", false);
        MAX_STACK_SIZE = BUILDER.comment(
                        "最大物品堆叠大小 (1-9999)",
                        "Maximum item stack size (1-9999)")
                .defineInRange("maxStackSize",64 , 1, 9999);
        BUILDER.pop(); // 自定义堆叠

        BUILDER.push("物品列表 | Item Lists");
        listMode = BUILDER.comment(
                        "0: 禁用 1: 白名单模式 2: 黑名单模式",
                        "0: Disabled, 1: Whitelist, 2: Blacklist")
                .defineInRange("listMode", 0, 0, 2);
        itemList = BUILDER.comment(
                        "白名单/黑名单中的物品注册名列表",
                        "Item registry names for whitelist/blacklist")
                .defineList("itemList", Collections.emptyList(), o -> o instanceof String);
        BUILDER.pop(); // 物品列表

        BUILDER.push("物品实体 | Item Entities");
        optimizeItems = BUILDER.comment(
                        "优化物品实体tick",
                        "Optimize item entity ticking")
                .define("optimizeItems", false);
        itemWhitelist = BUILDER.comment(
                        "物品实体白名单",
                        "Item entity whitelist")
                .defineList("itemWhitelist", List.of("minecraft:diamond"), o -> true);
        BUILDER.pop(); // 物品实体

        BUILDER.pop(); // 物品优化

        // ==================== 内存优化设置 | Memory Optimization Settings ====================
        BUILDER.comment("内存优化 | Memory Optimization").push("memory_optimization");

        MEMORY_CLEAN_INTERVAL = BUILDER.comment(
                        "内存清理间隔(秒)",
                        "Memory cleanup interval (seconds)")
                .defineInRange("cleanInterval", 600, 60, 3600);

        ENABLE_GC = BUILDER.comment(
                        "清理时触发垃圾回收",
                        "Trigger garbage collection during cleanup")
                .define("enableGC", false);

        BUILDER.pop(); // 内存优化

        // ==================== 渐进加载设置 | Progressive Loading Settings ====================
        BUILDER.comment("渐进加载 | Progressive Loading").push("progressive_loading");

        ENTITY_LOAD_BATCH_SIZE = BUILDER.comment(
                        "每tick加载的实体数量",
                        "Number of entities to load per tick")
                .defineInRange("entityLoadBatchSize", 10, 1, 100);

        TILE_ENTITY_LOAD_BATCH_SIZE = BUILDER.comment(
                        "每tick加载的方块实体数量",
                        "Number of tile entities to load per tick")
                .defineInRange("tileEntityLoadBatchSize", 5, 1, 50);

        MIN_TICK_INTERVAL = BUILDER.comment(
                        "实体更新最小间隔(ms)",
                        "Minimum tick interval (ms) between entity updates")
                .defineInRange("minTickInterval", 10, 1, 100);

        MAX_TICK_INTERVAL = BUILDER.comment(
                        "实体更新最大间隔(ms)",
                        "Maximum tick interval (ms) between entity updates")
                .defineInRange("maxTickInterval", 100, 10, 1000);

        TPS_ADJUST_THRESHOLD = BUILDER.comment(
                        "调整tick速率的TPS阈值（20的百分比）",
                        "TPS threshold for adjusting tick rate (percentage of 20)")
                .defineInRange("tpsAdjustThreshold", 0.85, 0.1, 1.0);

        BUILDER.pop(); // 渐进加载

        // ==================== 区块优化设置 | Chunk Optimization Settings ====================
        BUILDER.comment("区块优化 | Chunk Optimization").push("chunk_optimization");

        aggressiveChunkUnloading = BUILDER.comment(
                        "主动卸载非活动区块",
                        "Aggressively unload inactive chunks")
                .define("aggressiveChunkUnloading", false);

        chunkUnloadDelay = BUILDER.comment(
                        "区块卸载延迟 (秒)",
                        "Chunk unload delay (seconds)")
                .defineInRange("chunkUnloadDelay", 60, 10, 600);

        reduceChunkUpdates = BUILDER.comment(
                        "玩家短距离移动时减少区块更新",
                        "Reduce chunk updates when player moves short distances")
                .define("reduceChunkUpdates", true);



        CHUNK_GEN_THREADS = BUILDER.comment(
                        "异步区块生成线程数",
                        "Number of threads for async chunk generation")
                .defineInRange("chunkGenThreads", 6, 1, 8);

        BUILDER.pop(); // 区块优化

        // ==================== 异步优化设置 | Async Optimization Settings ====================
        BUILDER.comment("异步优化 | Async Optimization").push("async_optimization");

        ASYNC_PARTICLES = BUILDER.comment(
                        "启用异步粒子处理",
                        "Enable asynchronous particle processing")
                .define("asyncParticles", true);

        MAX_ASYNC_OPERATIONS_PER_TICK = BUILDER.comment(
                        "每tick最大异步操作数",
                        "Max async operations processed per tick")
                .defineInRange("maxAsyncOpsPerTick", 1000, 100, 10000);

        DISABLE_ASYNC_ON_ERROR = BUILDER.comment(
                        "出错后禁用该事件类型的异步处理",
                        "Disable async for event type after errors")
                .define("disableAsyncOnError", true);

        ASYNC_EVENT_TIMEOUT = BUILDER.comment(
                        "异步事件超时时间(秒)",
                        "Timeout in seconds for async events")
                .defineInRange("asyncEventTimeout", 2, 1, 10);

        WAIT_FOR_ASYNC_EVENTS = BUILDER.comment(
                        "等待异步事件完成",
                        "Wait for async events to complete")
                .define("waitForAsyncEvents", false);

        BUILDER.push("线程配置 | Thread Configuration");
        maxCPUPro = BUILDER.comment(
                        "异步系统最大CPU核心数",
                        "Max CPU Cores for async system")
                .defineInRange("maxCPUPro", 8, 2, 9999);
        maxthreads = BUILDER.comment(
                        "最大线程数",
                        "Max Threads for general async operations")
                .defineInRange("maxthreads", 8, 2, 9999);
        BUILDER.pop(); // 线程配置

        BUILDER.pop(); // 异步优化

        // ==================== 事件系统设置 | Event System Settings ====================
        BUILDER.comment("事件系统 | Event System").push("event_system");

        FEATURE_ENABLED = BUILDER.comment(
                        "启用高性能异步事件功能",
                        "Enable high-performance async event system")
                .define("featureEnabled", true);

        ASYNC_EVENT_CLASS_BLACKLIST = BUILDER.comment(
                        "不应异步处理的事件类列表（支持通配符）",
                        "Event classes that should NOT be processed asynchronously (supports wildcards)")
                .defineList("classBlacklist",
                        List.of(
                                "net.minecraftforge.event.TickEvent",
                                "net.minecraftforge.event.level.LevelTickEvent",
                                "net.minecraftforge.event.entity.living.*"
                        ),
                        o -> o instanceof String);

        ASYNC_EVENT_MOD_BLACKLIST = BUILDER.comment(
                        "不应异步处理的模组ID列表",
                        "Mod IDs whose events should NOT be processed asynchronously")
                .defineList("modBlacklist", Collections.emptyList(), o -> o instanceof String);

        STRICT_CLASS_CHECKING = BUILDER.comment(
                        "启用严格的类存在检查（推荐开启以确保稳定性）",
                        "Enable strict class existence checking (disable for stability)")
                .define("strictClassChecking", true);

        BUILDER.pop(); // 事件系统

        // ==================== 调试设置 | Debug Settings ====================
        BUILDER.comment("调试选项 | Debug Options").push("debug");

        DEBUG_LOGGING = BUILDER.comment(
                        "启用调试日志",
                        "Enable debug logging")
                .define("debug", false);

        LOG_BLOCK_EVENTS = BUILDER.comment(
                        "记录方块相关事件",
                        "Log block related events")
                .define("logBlockEvents", false);

        BUILDER.pop(); // 调试选项

        // ==================== 其他设置 | Miscellaneous Settings ====================
        BUILDER.comment("其他设置 | Miscellaneous").push("miscellaneous");

        sendLoginMessage = BUILDER.comment(
                        "玩家登录时发送消息",
                        "Send message on player login")
                .define("sendLoginMessage", true);

        dimensionWhitelist = BUILDER.comment(
                        "维度白名单",
                        "Dimension whitelist")
                .defineList("dimensionWhitelist", Collections.emptyList(), o -> o instanceof String);

        BUILDER.pop(); // 其他设置

        SPEC = BUILDER.build();
    }

    // ==================== 工具方法 | Utility Methods ====================
    public static int getCullingDepth() {
        return CULLING_DEPTH.get();
    }

    public static boolean useUltraCulling() {
        return ULTRA_CULLING.get();
    }

    public static int ultraCullingDepth() {
        return ULTRA_DEPTH.get();
    }

    public static double ultraBackfaceThreshold() {
        return ULTRA_BACKFACE.get();
    }

    public static float getRejectionRate() {
        return REJECTION_RATE.get().floatValue();
    }

    public static boolean optimizeMangrove() {
        return OPTIMIZE_MANGROVE.get();
    }

    public static boolean useAsyncTracing() {
        return useAsyncTracing.get();
    }

    public static double getTraceDistance() {
        return traceDistance.get();
    }

    public static boolean useAdvancedCulling() {
        return ADVANCED_CULLING.get();
    }

    public static boolean forceMetal() {
        return FORCE_METAL.get();
    }

    public static Set<String> getAsyncEventClassBlacklist() {
        return new HashSet<>(ASYNC_EVENT_CLASS_BLACKLIST.get());
    }

    public static boolean isEnabled() {
        return FEATURE_ENABLED.get();
    }

    public static Set<String> getAsyncEventModBlacklist() {
        return new HashSet<>(ASYNC_EVENT_MOD_BLACKLIST.get());
    }
    public enum RenderMode {
        SIMPLE, VANILLA, SEMI_COMPLEX
    }
    public static boolean isStrictClassCheckingEnabled() {
        return STRICT_CLASS_CHECKING.get();
    }

    public static void resetToDefault() {
        ForgeConfigSpec newSpec = new ForgeConfigSpec.Builder()
                .configure(builder -> {
                    new CoolConfig();
                    return builder.build();
                }).getRight();
        SPEC = newSpec;
        newSpec.save();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "mpem.toml");
    }

    public static void onChange(Consumer<Void> listener) {
        changeListener = listener;
    }
}