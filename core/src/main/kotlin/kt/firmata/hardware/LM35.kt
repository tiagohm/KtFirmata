package kt.firmata.hardware

import kt.firmata.core.IOEvent
import kt.firmata.core.OnPinChangeListener
import kt.firmata.core.OnStopListener
import kt.firmata.core.Pin
import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.message.ReportAnalogPin
import java.time.Duration

data class LM35(val board: Board, val pin: Pin, val aref: Double = 5.0) : Thermometer<LM35>, OnPinChangeListener {

    private val thermometerListeners = HashSet<ThermometerListener<LM35>>()

    @Volatile private var value = 0
    @Volatile private var freqTime = 0L
    @Volatile private var prevTime = 0L

    @Volatile override var temperature = 0.0
        private set

    override val name = "LM35"

    init {
        board.addEventListener(OnStopListener { close() })
    }

    override fun registerThermometerListener(listener: ThermometerListener<LM35>) {
        thermometerListeners.add(listener)
    }

    override fun unregisterThermometerListener(listener: ThermometerListener<LM35>) {
        thermometerListeners.remove(listener)
    }

    @Synchronized
    override fun start(freq: Duration) {
        freqTime = freq.toMillis()
        board.sendMessage(ReportAnalogPin(pin, true))
        board.addEventListener(this)
    }

    @Synchronized
    override fun close() {
        board.removeEventListener(this)
        board.sendMessage(ReportAnalogPin(pin, false))
    }

    override fun accept(event: IOEvent) {
        if (value != event.value &&
            (freqTime <= 0L || System.currentTimeMillis() - prevTime >= freqTime) &&
            event.pin === pin
        ) {
            value = event.value
            prevTime = System.currentTimeMillis()
            temperature = aref * 1000.0 * event.value / 10230.0
            thermometerListeners.forEach { it.onTemperatureChange(this) }
        }
    }
}
