package net.fakefabricmc.fabric.api.client.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientTickEvents {
    public ClientTickEvents() {
    }

    /**
     * Called at the start of the client tick.
     */
    public static final Event<StartTick> START_CLIENT_TICK = EventFactory.createArrayBacked(StartTick.class, callbacks -> client -> {
        if (EventFactory.isProfilingEnabled()) {
            final Profiler profiler = client.getProfiler();
            profiler.push("fabricStartClientTick");

            for (StartTick event : callbacks) {
                profiler.push(EventFactory.getHandlerName(event));
                event.onStartTick(client);
                profiler.pop();
            }

            profiler.pop();
        } else {
            for (StartTick event : callbacks) {
                event.onStartTick(client);
            }
        }
    });

    /**
     * Called at the end of the client tick.
     */
    public static final Event<EndTick> END_CLIENT_TICK = EventFactory.createArrayBacked(EndTick.class, callbacks -> client -> {
        if (EventFactory.isProfilingEnabled()) {
            final Profiler profiler = client.getProfiler();
            profiler.push("fabricEndClientTick");

            for (EndTick event : callbacks) {
                profiler.push(EventFactory.getHandlerName(event));
                event.onEndTick(client);
                profiler.pop();
            }

            profiler.pop();
        } else {
            for (EndTick event : callbacks) {
                event.onEndTick(client);
            }
        }
    });

    /**
     * Called at the start of a ClientWorld's tick.
     */
    public static final Event<StartWorldTick> START_WORLD_TICK = EventFactory.createArrayBacked(StartWorldTick.class, callbacks -> world -> {
        if (EventFactory.isProfilingEnabled()) {
            final Profiler profiler = world.getProfiler();
            profiler.push("fabricStartClientWorldTick");

            for (StartWorldTick callback : callbacks) {
                profiler.push(EventFactory.getHandlerName(callback));
                callback.onStartTick(world);
                profiler.pop();
            }

            profiler.pop();
        } else {
            for (StartWorldTick callback : callbacks) {
                callback.onStartTick(world);
            }
        }
    });

    /**
     * Called at the end of a ClientWorld's tick.
     *
     * <p>End of world tick may be used to start async computations for the next tick.
     */
    public static final Event<EndWorldTick> END_WORLD_TICK = EventFactory.createArrayBacked(EndWorldTick.class, callbacks -> world -> {
        if (EventFactory.isProfilingEnabled()) {
            final Profiler profiler = world.getProfiler();
            profiler.push("fabricEndClientWorldTick");

            for (EndWorldTick callback : callbacks) {
                profiler.push(EventFactory.getHandlerName(callback));
                callback.onEndTick(world);
                profiler.pop();
            }

            profiler.pop();
        } else {
            for (EndWorldTick callback : callbacks) {
                callback.onEndTick(world);
            }
        }
    });

    @FunctionalInterface
    public interface StartTick {
        void onStartTick(MinecraftClient client);
    }

    @FunctionalInterface
    public interface EndTick {
        void onEndTick(MinecraftClient client);
    }

    @FunctionalInterface
    public interface StartWorldTick {
        void onStartTick(ClientWorld world);
    }

    @FunctionalInterface
    public interface EndWorldTick {
        void onEndTick(ClientWorld world);
    }
}
