package net.shuyanmc.mpem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.shuyanmc.mpem.async.AsyncSystemInitializer;
import net.shuyanmc.mpem.client.ItemCountRenderer;
import net.shuyanmc.mpem.config.CoolConfig;
import net.shuyanmc.mpem.engine.cull.RenderOptimizer;
import net.shuyanmc.mpem.events.ModEventHandlers;
import net.shuyanmc.mpem.particles.AsyncParticleHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.Configuration;
import org.spongepowered.asm.launch.MixinBootstrap;
import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.util.concurrent.atomic.AtomicBoolean;
@Mod(value = "mpem")
public class MpemMod {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "mpem";
	public static final String VERSION = "3.0.3";
	private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
	public static File MPEM_EVENTS_LOG = new File("log/mpem-event-debug.log");
	public MpemMod() {

		var bus = FMLJavaModLoadingContext.get().getModEventBus();
		LOGGER.info("MPEM主类正在加载");
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CoolConfig.SPEC);
		MinecraftForge.EVENT_BUS.register(this);
		modEventBus.addListener(this::setup);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			MinecraftForge.EVENT_BUS.register(ItemCountRenderer.class);
		});
		MixinBootstrap.init();
		ModEventHandlers.register(modEventBus, forgeEventBus);
		modEventBus.addListener(AsyncSystemInitializer::init);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			AsyncEventSystem.shutdown();
			AsyncParticleHandler.shutdown();
		}));
		LOGGER.info("Initializing MPEM MOD v{}", VERSION);
		LOGGER.info("MPEM模组其它功能启动中...");
		LOGGER.info("SYMC玩家QQ交流群：372378451");
		ModLoadingContext.get().registerExtensionPoint(
				IExtensionPoint.DisplayTest.class,
				() -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true)
		);
	}
	/**
	 * @author Red flag with 5 stars--RedStar
	 * @reason 检测
	 */
	private void setupClient(final FMLClientSetupEvent event) {

		RenderOptimizer.initialize();
		Runtime.getRuntime().addShutdownHook(new Thread(RenderOptimizer::shutdown));
		if (isMacOS()) {
			enableMetalBackend();
		}
	}
	private boolean isMacOS() {
		return System.getProperty("os.name").toLowerCase().contains("mac");
	}

	/**
	 * @author SYMC-Red flag with 5 stars--RedStar
	 * @reason 避免临时 Vector4f
	 */
	private void enableMetalBackend() {
		try {
			Configuration.LIBRARY_PATH.set("metal");
			System.setProperty("org.lwjgl.metal.libraryname", "Metal");
			System.out.println("Metal backend enabled");
			System.setProperty("org.lwjgl.opengl.enabled", "false");
		} catch (Exception e) {
			System.err.println("Failed to enable Metal backend: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static int getMaxWorkerThreads() {
		String propValue = System.getProperty("max.worker.threads");
		try {
			if (propValue != null) {
				int value = Integer.parseInt(propValue);
				return Math.max(1, Math.min(value, 32767));
			}
		} catch (NumberFormatException ignored) {}
		return Math.min(32767, Runtime.getRuntime().availableProcessors() * 2);
	}

	private void setup(final FMLCommonSetupEvent event) {

		LOGGER.info("MPEM Mod 初始化完成");
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		LOGGER.info("Loading MPEM native library...");
		event.enqueueWork(() -> {
			AsyncEventSystem.initialize();
			ModEventProcessor.processModEvents();

		});
	}
	@OnlyIn(Dist.CLIENT)
	public static void registerDynamicListener(
			Class<? extends Event> eventType,
			IEventListener listener,
			EventPriority priority,
			boolean receiveCancelled
	) {
		MinecraftForge.EVENT_BUS.addListener(
				priority,
				receiveCancelled,
				eventType,
				event -> {
					try {
						listener.invoke(event);
					} catch (ClassCastException e) {
						LOGGER.error("Event type mismatch for listener", e);
					} catch (Throwable t) {
						LOGGER.error("Error in optimized event handler", t);
						if (CoolConfig.DISABLE_ASYNC_ON_ERROR.get() && eventType.getSimpleName().contains("Async")) {
							LOGGER.warn("Disabling async for event type due to handler error: {}", eventType.getName());
							AsyncEventSystem.registerSyncEvent(eventType);
						}
					}
				}
		);
	}
	public static void executeSafeAsync(Runnable task, String taskName) {
		AsyncEventSystem.executeAsync(
				TickEvent.ServerTickEvent.class,
				() -> {
					try {
						long start = System.currentTimeMillis();
						task.run();
						long duration = System.currentTimeMillis() - start;
						if (duration > 100) {
							LOGGER.debug("Async task '{}' completed in {}ms", taskName, duration);
						}
					} catch (Throwable t) {
						LOGGER.error("Async task '{}' failed", taskName, t);
						throw t;
					}
				}
		);
	}
}