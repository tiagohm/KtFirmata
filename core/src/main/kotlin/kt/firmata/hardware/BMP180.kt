package kt.firmata.hardware

import kt.firmata.core.IODevice
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.pow

// https://cdn-shop.adafruit.com/datasheets/BST-BMP180-DS000-09.pdf
// https://github.com/dotnet/iot/blob/main/src/devices/Bmp180/Bmp180.cs
// https://github.com/rwaldron/johnny-five/blob/main/lib/barometer.js#L45
// https://github.com/adafruit/Adafruit-BMP085-Library

// Tested with Arduino + Adafruit-BMP085-Library
// ac1 = 8167
// ac2 = -1146
// ac3 = -14574
// ac4 = 34285
// ac5 = 24212
// ac6 = 23458
// b1 = 5498
// b2 = 61
// mb = -32768
// mc = -11075
// md = 2432
// Temperature = Raw temp: 32774
// 27.80 *C
// Pressure = Raw temp: 32772
// Raw pressure: 312855
// X1 = 0
// X2 = 0
// B5 = 4447
// B6 = 447
// X1 = 1
// X2 = -251
// B3 = 64836
// X1 = -796
// X2 = 4
// B4 = 34077
// B7 = 1550118750
// p = 90977
// X1 = 5842
// X2 = -10213
// p = 90940
// 90940 Pa

class BMP180(val board: IODevice, val mode: Mode = Mode.ULTRA_LOW_POWER) : Barometer<BMP180>, Thermometer<BMP180>, Runnable {

    @Suppress("PropertyName")
    data class CalibrationData(
        @JvmField var AC1: Int = 408,
        @JvmField var AC2: Int = -72,
        @JvmField var AC3: Int = -14383,
        @JvmField var AC4: Int = 32741,
        @JvmField var AC5: Int = 32757,
        @JvmField var AC6: Int = 23153,

        @JvmField var B1: Int = 6190,
        @JvmField var B2: Int = 4,

        @JvmField var MB: Int = -32768,
        @JvmField var MC: Int = -8711,
        @JvmField var MD: Int = 2868,
    )

    enum class Mode(@JvmField val sleepTimeInMilliseconds: Long) {
        ULTRA_LOW_POWER(5L),
        STANDARD(8L),
        HIGH_RESOLUTION(14L),
        ULTRA_HIGH_RESOLUTION(26L),
    }

    private val device = board.i2CDevice(ADDRESS)
    private val calibrationData = CalibrationData()

    private val thermometerListeners = HashSet<ThermometerListener<BMP180>>()
    private val barometerListeners = HashSet<BarometerListener<BMP180>>()
    private val altimeterListeners = HashSet<AltimeterListener<BMP180>>()

    @Volatile private var task: ScheduledFuture<*>? = null

    override val name = "BMP180"

    @Volatile override var pressure = 0.0
        private set

    @Volatile override var altitude = 0.0
        private set

    @Volatile override var temperature = 0.0
        private set

    override fun registerThermometerListener(listener: ThermometerListener<BMP180>) {
        thermometerListeners.add(listener)
    }

    override fun unregisterThermometerListener(listener: ThermometerListener<BMP180>) {
        thermometerListeners.remove(listener)
    }

    override fun registerBarometerListener(listener: BarometerListener<BMP180>) {
        barometerListeners.add(listener)
    }

    override fun unregisterBarometerListener(listener: BarometerListener<BMP180>) {
        barometerListeners.remove(listener)
    }

    override fun registerAltimeterListener(listener: AltimeterListener<BMP180>) {
        altimeterListeners.add(listener)
    }

    override fun unregisterAltimeterListener(listener: AltimeterListener<BMP180>) {
        altimeterListeners.remove(listener)
    }

    @Synchronized
    override fun start(period: Duration) {
        if (task == null) {
            device.ask(COEFFICIENTS_REG, 22) {
                with(calibrationData) {
                    AC1 = ((it.data[0] shl 8) or it.data[1]).toShort().toInt()
                    AC2 = ((it.data[2] shl 8) or it.data[3]).toShort().toInt()
                    AC3 = ((it.data[4] shl 8) or it.data[5]).toShort().toInt()
                    AC4 = (it.data[6] shl 8) or it.data[7]
                    AC5 = (it.data[8] shl 8) or it.data[9]
                    AC6 = (it.data[10] shl 8) or it.data[11]
                    B1 = ((it.data[12] shl 8) or it.data[13]).toShort().toInt()
                    B2 = ((it.data[14] shl 8) or it.data[15]).toShort().toInt()
                    MB = ((it.data[16] shl 8) or it.data[17]).toShort().toInt()
                    MC = ((it.data[18] shl 8) or it.data[19]).toShort().toInt()
                    MD = ((it.data[20] shl 8) or it.data[21]).toShort().toInt()

                    LOG.info(
                        "Calibration data received. AC1={}, AC2={}, AC3={}, AC4={}, AC5={}, AC6={}, B1={}, B2={}, MB={}, MC={}, MD={}",
                        AC1, AC2, AC3, AC4, AC5, AC6, B1, B2, MB, MC, MD
                    )

                    task = HardwareScheduler
                        .scheduleAtFixedRate(this@BMP180, 1000L, max(MIN_DELAY.toMillis(), period.toMillis()), TimeUnit.MILLISECONDS)
                }
            }
        }
    }

    override fun run() {
        if (thermometerListeners.isNotEmpty() || barometerListeners.isNotEmpty() || altimeterListeners.isNotEmpty()) {
            val b5 = calculateTrueTemperature()
            temperature = (b5 + 8) / 160.0

            val b6 = b5 - 4000
            val k = (b6 * b6) / 4096
            var x3 = ((calibrationData.B2 * k) + (calibrationData.AC2 * b6)) / 2048
            val b3 = (((calibrationData.AC1 * 4 + x3) shl mode.ordinal) + 2) / 4
            var x1 = (calibrationData.AC3 * b6) / 8192
            val x2 = (calibrationData.B1 * k) / 65536
            x3 = (x1 + x2 + 2) / 4
            val b4 = calibrationData.AC4 * ((x3 + 32768).toLong() and 0xFFFFFFFF) / 32768
            val up = readUncompensatedPressure().get(MIN_DELAY.toMillis(), TimeUnit.MILLISECONDS)
            val b7 = ((up - b3).toLong() and 0xFFFFFFFF) * (50000 shr mode.ordinal)
            val p = if ((b7 < 0x80000000)) (b7 * 2) / b4 else (b7 / b4) * 2
            x1 = ((((p * p) / 65536) * 3038) / 65536).toInt()
            pressure = (p + (x1 + ((-7357 * p) / 65536) + 3791) / 8).toDouble()

            altitude = 44330 * (1.0 - (pressure / 101325).pow(1.0 / 5.255))

            thermometerListeners.forEach { it.onTemperatureChange(this) }
            barometerListeners.forEach { it.onPressureChange(this) }
            altimeterListeners.forEach { it.onAltitudeChange(this) }
        }
    }

    private fun readUncompensatedTemperature(): Future<Int> {
        device.tell(READ_TEMP)

        Thread.sleep(5)

        val result = CompletableFuture<Int>()

        device.ask(TEMP_DATA_REG, 2) {
            result.complete((it.data[0] shl 8) or it.data[1])
        }

        return result
    }

    private fun readUncompensatedPressure(): Future<Int> {
        device.tell(byteArrayOf(CONTROL_REG.toByte(), (READ_PRES_CMD or (mode.ordinal shl 6)).toByte()))

        Thread.sleep(mode.sleepTimeInMilliseconds)

        val result = CompletableFuture<Int>()

        device.ask(PRES_DATA_REG, 3) {
            result.complete(((it.data[0] shl 16) or (it.data[1] shl 8) or it.data[2]) shr (8 - mode.ordinal))
        }

        return result
    }

    private fun calculateTrueTemperature(): Int {
        val ut = readUncompensatedTemperature().get(MIN_DELAY.toMillis(), TimeUnit.MILLISECONDS)

        // Calculations below are taken straight from section 3.5 of the datasheet.
        val x1 = (ut - calibrationData.AC6) * calibrationData.AC5 / 32768
        val x2 = calibrationData.MC * 2048 / (x1 + calibrationData.MD)

        return x1 + x2
    }

    @Synchronized
    override fun close() {
        task?.cancel(true)
        task = null
    }

    companion object {

        const val ADDRESS = 0x77

        private const val COEFFICIENTS_REG = 0xAA
        private const val CONTROL_REG = 0xF4
        private const val TEMP_DATA_REG = 0xF6
        private const val PRES_DATA_REG = 0xF6

        private const val READ_TEMP_CMD = 0x2E
        private const val READ_PRES_CMD = 0x34

        @JvmStatic private val LOG = LoggerFactory.getLogger(BMP180::class.java)
        @JvmStatic private val MIN_DELAY = Duration.ofSeconds(1L)

        @JvmStatic private val READ_TEMP = byteArrayOf(CONTROL_REG.toByte(), READ_TEMP_CMD.toByte())
    }
}
