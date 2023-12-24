package kt.firmata.hardware

import kt.firmata.core.*
import kt.firmata.core.protocol.message.ReportAnalogPin
import org.apache.commons.lang3.time.StopWatch
import java.time.Duration

data class LM35(val board: IODevice, val pin: Pin, val aref: Double = 5.0) : Thermometer<LM35>, OnPinChangeListener, Runnable {

    private val thermometerListeners = HashSet<ThermometerListener<LM35>>()
    private val stopWatch = StopWatch()

    @Volatile private var freqTime = 0L
    @Volatile private var firstTime = true

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
    override fun start(period: Duration) {
        freqTime = period.toMillis()
        stopWatch.start()
        board.sendMessage(ReportAnalogPin(pin, true))
        board.addEventListener(this)
    }

    override fun run() {
        temperature = aref * 1000.0 * pin.value / 10230.0
        thermometerListeners.forEach { it.onTemperatureChange(this) }
    }

    @Synchronized
    override fun close() {
        board.removeEventListener(this)
        board.sendMessage(ReportAnalogPin(pin, false))
    }

    override fun accept(event: IOEvent) {
        if (thermometerListeners.isNotEmpty() && event.pin === pin && (firstTime || stopWatch.time >= freqTime)) {
            firstTime = false

            run()

            stopWatch.reset()
            stopWatch.start()
        }
    }
}
