package net.shuyanmc.mpem;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.shuyanmc.mpem.config.CoolConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class AsyncEvent extends Event {

    private final boolean async;
    private EventPriority priority = EventPriority.NORMAL;
    private volatile boolean completed = false;
    private volatile boolean success = false;
    private Throwable failureCause = null;

    protected AsyncEvent(boolean async) {
        this.async = async;
    }

    protected AsyncEvent(boolean async, EventPriority priority) {
        this.async = async;
        this.priority = priority;
    }

    public boolean isAsync() {
        return async;
    }

    public EventPriority getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isSuccess() {
        return success;
    }

    public Throwable getFailureCause() {
        return failureCause;
    }

    public void post() {
        if (!CoolConfig.isEnabled()) {
            return;
        }
        if (async) {
            CompletableFuture<Void> future = AsyncEventSystem.executeAsync(this.getClass(), () -> {
                try {
                    long startTime = System.currentTimeMillis();
                    MinecraftForge.EVENT_BUS.post(this);
                    success = true;
                    long duration = System.currentTimeMillis() - startTime;
                    if (duration > 100) {
                        AsyncEventSystem.LOGGER.warn("Async event {} took {}ms to process",
                                this.getClass().getSimpleName(), duration);
                    }
                } catch (Throwable t) {
                    failureCause = t;
                    success = false;
                    AsyncEventSystem.LOGGER.error("Async event handling failed", t);
                    throw t;
                } finally {
                    completed = true;
                }
            });

            if (CoolConfig.WAIT_FOR_ASYNC_EVENTS.get()) {
                try {
                    future.get(CoolConfig.ASYNC_EVENT_TIMEOUT.get(), TimeUnit.SECONDS);
                } catch (Exception e) {
                    AsyncEventSystem.LOGGER.warn("Async event timed out or interrupted", e);
                }
            }
        } else {
            try {
                long startTime = System.currentTimeMillis();
                MinecraftForge.EVENT_BUS.post(this);
                success = true;
                long duration = System.currentTimeMillis() - startTime;
                if (duration > 50) {
                    AsyncEventSystem.LOGGER.debug("Sync event {} took {}ms to process",
                            this.getClass().getSimpleName(), duration);
                }
            } catch (Throwable t) {
                failureCause = t;
                success = false;
                AsyncEventSystem.LOGGER.error("Sync event handling failed", t);
                throw t;
            } finally {
                completed = true;
            }
        }
    }

    public void registerToBus(IEventBus bus) {
        bus.addListener(this.getPriority(), false, this.getClass(), this::handleEventWrapper);
    }

    private void handleEventWrapper(Event event) {
        if (!CoolConfig.isEnabled()) {
            return;
        }
        if (!(event instanceof AsyncEvent)) {
            return;
        }
        AsyncEvent asyncEvent = (AsyncEvent) event;
        try {
            handleEvent(asyncEvent);
        } catch (Throwable t) {
            AsyncEventSystem.LOGGER.error("Error in async event handler", t);
            if (CoolConfig.DISABLE_ASYNC_ON_ERROR.get()) {
                AsyncEventSystem.LOGGER.warn("Disabling async for event type due to handler error: {}",
                        event.getClass().getName());
                AsyncEventSystem.registerSyncEvent(event.getClass());
            }
            throw t;
        }
    }

    protected abstract void handleEvent(AsyncEvent event);

    public static void waitForCompletion(AsyncEvent event) {
        if (!CoolConfig.isEnabled()) {
            return;
        }
        if (event == null || !event.isAsync()) {
            return;
        }

        while (!event.isCompleted()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public static void waitForCompletion(AsyncEvent... events) {
        if (!CoolConfig.isEnabled()) {
            return;
        }
        if (events == null) {
            return;
        }

        for (AsyncEvent event : events) {
            waitForCompletion(event);
        }
    }
}