package kt.firmata.core.protocol

import kt.firmata.core.protocol.fsm.Event
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import kotlin.concurrent.Volatile

data class FirmataWatchdog(
    private val timeout: Duration,
    private val action: Runnable,
) : Consumer<Event> {

    private val active = AtomicBoolean(false)

    @Volatile private var lastTimestamp = 0L

    init {
        require(timeout.toMillis() > 0) { "timeout must be > 0ms" }
    }

    override fun accept(event: Event) {
        if (lastTimestamp == 0L) {
            enable()
        }

        lastTimestamp = System.currentTimeMillis()
    }

    fun isActive(): Boolean {
        return active.get()
    }

    fun setActive(active: Boolean) {
        if (active) {
            enable()
        } else {
            disable()
        }
    }

    fun enable() {
        if (!active.getAndSet(true)) {
            EXECUTOR.schedule(Watcher(), timeout.toMillis(), TimeUnit.MILLISECONDS)
        }
    }

    fun disable() {
        active.set(false)
    }

    private inner class Watcher : Runnable {

        override fun run() {
            if (System.currentTimeMillis() - lastTimestamp >= timeout.toMillis()) {
                action.run()
            }

            if (active.get()) {
                EXECUTOR.schedule(this, timeout.toMillis(), TimeUnit.MILLISECONDS)
            }
        }
    }

    companion object {

        @JvmStatic private val EXECUTOR = Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory("Firmata-Watchdog"))
    }
}
