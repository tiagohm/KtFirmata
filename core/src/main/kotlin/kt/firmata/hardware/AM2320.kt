package kt.firmata.hardware

import kt.firmata.core.I2CEvent
import kt.firmata.core.I2CListener
import kt.firmata.core.OnStopListener
import kt.firmata.core.protocol.board.Board
import java.time.Duration
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.max

// https://cdn-shop.adafruit.com/product-files/3721/AM2320.pdf
// https://github.com/dotnet/iot/blob/main/src/devices/Am2320/Am2320.cs

class AM2320(val board: Board) : Thermometer, Hygrometer, Runnable, I2CListener {

    private val thermometerListeners = HashSet<ThermometerListener<AM2320>>()
    private val hygrometerListeners = HashSet<HygrometerListener<AM2320>>()

    private val device = board.i2CDevice(ADDRESS)
    @Volatile private var task: ScheduledFuture<*>? = null

    @Volatile override var humidity = 0.0
        private set

    @Volatile override var temperature = 0.0
        private set

    init {
        board.addEventListener(OnStopListener { stop() })
    }

    fun registerThermometerListener(listener: ThermometerListener<AM2320>) {
        thermometerListeners.add(listener)
    }

    fun unregisterThermometerListener(listener: ThermometerListener<AM2320>) {
        thermometerListeners.remove(listener)
    }

    fun registerHygrometerListener(listener: HygrometerListener<AM2320>) {
        hygrometerListeners.add(listener)
    }

    fun unregisterHygrometerListener(listener: HygrometerListener<AM2320>) {
        hygrometerListeners.remove(listener)
    }

    override fun run() {
        if (thermometerListeners.isNotEmpty() || hygrometerListeners.isNotEmpty()) {
            // Wake up.
            device.tell(WAKE_UP)

            Thread.sleep(10)

            // Send a command to read register.
            device.tell(READ)

            Thread.sleep(1)

            // 2 bytes preamble, 4 bytes data, 2 bytes CRC.
            device.ask(8, this)
        }
    }

    override fun onReceive(event: I2CEvent) {
        if (event.data[0] == READ_REGISTER_CMD.toByte()) {
            humidity = ((event.data[2].toInt() and 0xFF shl 8) or (event.data[3].toInt() and 0xFF)) / 10.0
            temperature = ((event.data[4].toInt() and 0x7F shl 8) or (event.data[5].toInt() and 0xFF)) / 10.0
            if (event.data[4].toInt() and 0x80 != 0) temperature = -temperature

            thermometerListeners.forEach { it.onTemperatureChange(this) }
            hygrometerListeners.forEach { it.onHumidityChange(this) }
        }
    }

    @Synchronized
    override fun start(freq: Duration) {
        if (task == null) {
            device.subscribe(this)
            task = HardwareScheduler.scheduleAtFixedRate(this, 0L, max(MIN_DELAY.toMillis(), freq.toMillis()), TimeUnit.MILLISECONDS)
        }
    }

    @Synchronized
    override fun stop() {
        device.unsubscribe(this)
        task?.cancel(true)
        task = null
    }

    companion object {

        const val ADDRESS = 0x5C

        private const val READ_REGISTER_CMD = 0x03
        private const val REG_TEMP_H = 0x02
        private const val REG_HUM_H = 0x00

        @JvmStatic private val WAKE_UP = byteArrayOf(0)
        @JvmStatic private val READ = byteArrayOf(READ_REGISTER_CMD.toByte(), REG_HUM_H.toByte(), 4)

        @JvmStatic private val MIN_DELAY = Duration.ofSeconds(2L)
    }
}
