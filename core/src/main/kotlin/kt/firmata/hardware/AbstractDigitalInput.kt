package kt.firmata.hardware

import kt.firmata.core.IODevice
import kt.firmata.core.IOEvent
import kt.firmata.core.OnPinChangeListener
import kt.firmata.core.Pin
import kt.firmata.core.protocol.message.ReportDigitalPin
import java.time.Duration

abstract class AbstractDigitalInput<T : DigitalInput<T>> : DigitalInput<T>, OnPinChangeListener, Runnable {

    private val digitalInputListeners = HashSet<DigitalInputListener<T>>()

    abstract val board: IODevice

    abstract val pin: Pin

    override val value
        get() = pin.value != 0

    override val name
        get() = "D${pin.index}"

    override fun registerDigitalInputListener(listener: DigitalInputListener<T>) {
        digitalInputListeners.add(listener)
    }

    override fun unregisterDigitalInputListener(listener: DigitalInputListener<T>) {
        digitalInputListeners.remove(listener)
    }

    @Suppress("UNCHECKED_CAST")
    override fun run() {
        digitalInputListeners.forEach { it.onDigitalInputChange(this as T) }
    }

    override fun start(period: Duration) {
        board.sendMessage(ReportDigitalPin(pin, true))
        board.addEventListener(this)
    }

    override fun close() {
        board.sendMessage(ReportDigitalPin(pin, false))
        board.removeEventListener(this)
    }

    override fun accept(event: IOEvent) {
        if (digitalInputListeners.isNotEmpty() && event.pin === pin) {
            run()
        }
    }
}
