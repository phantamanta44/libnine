package io.github.phantamanta44.libnine.client.event;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collection;

public class ClientTickHandler {

    private static final Collection<Listener> listeners = new ArrayList<>();
    private static long tick;

    public static long getTick() {
        return tick;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.ClientTickEvent.Phase.END) {
            ++tick;
            for (Listener listener : listeners) {
                listener.onClientTick(tick);
            }
        }
    }

    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public interface Listener {

        void onClientTick(long tick);

    }

}
