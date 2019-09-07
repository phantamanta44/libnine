package xyz.phanta.libnine.client.event

import net.minecraftforge.event.TickEvent
import java.util.function.Consumer

class ClientTickHandler : Consumer<TickEvent.ClientTickEvent> {

    var tick: Long = 0L
        private set

    private val listeners: MutableList<Listener> = mutableListOf()

    fun registerListener(listener: Listener) {
        listeners += listener
    }

    override fun accept(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            ++tick
            listeners.forEach { it.onClientTick(tick) }
        }
    }

    interface Listener {

        fun onClientTick(tick: Long)

    }

}
