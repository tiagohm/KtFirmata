package kt.firmata.hardware

import kt.firmata.core.OnPinChangeListener
import kt.firmata.core.OnStopListener
import kt.firmata.core.Pin
import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.message.ReportAnalogPin
import java.time.Duration

data class LM35(val board: Board, val pin: Pin, val aref: Double = 5.0) : Thermometer {

    private val thermometerListeners = HashSet<ThermometerListener<LM35>>()

    @Volatile private var value = 0

    @Volatile override var temperature = 0.0
        private set

    private val onPinChange = OnPinChangeListener { event ->
        if (value != event.value && event.pin === pin) {
            value = event.value
            temperature = aref * 1000.0 * event.value / 10230.0
            thermometerListeners.forEach { it.onTemperatureChange(this) }
        }
    }

    init {
        board.addEventListener(OnStopListener { stop() })
    }

    fun registerThermometerListener(listener: ThermometerListener<LM35>) {
        thermometerListeners.add(listener)
    }

    fun unregisterThermometerListener(listener: ThermometerListener<LM35>) {
        thermometerListeners.remove(listener)
    }

    @Synchronized
    override fun start(freq: Duration) {
        board.sendMessage(ReportAnalogPin(pin, true))
        board.addEventListener(onPinChange)
    }

    @Synchronized
    override fun stop() {
        board.removeEventListener(onPinChange)
        board.sendMessage(ReportAnalogPin(pin, false))
    }
}
