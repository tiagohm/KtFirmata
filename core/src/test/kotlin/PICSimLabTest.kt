import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import kt.firmata.core.IODeviceEventListener
import kt.firmata.core.IOEvent
import kt.firmata.core.protocol.board.ArduinoUno
import kt.firmata.core.protocol.transport.SerialTransport
import kt.firmata.hardware.*
import java.time.Duration

class PICSimLabTest : StringSpec(), IODeviceEventListener, ThermometerListener<Thermometer>, HygrometerListener<Hygrometer> {

    init {
        "report" {
            val transport = SerialTransport("COM5", 115200)
            val board = ArduinoUno(transport)

            board.addEventListener(this@PICSimLabTest)

            board.start()
            board.ensureInitializationIsDone()

            val thermometer = LM35(board, board.pinAt(ArduinoUno.A0))
            thermometer.registerThermometerListener(this@PICSimLabTest)
            thermometer.start()

            val hygrometer = AM2320(board)
            hygrometer.registerThermometerListener(this@PICSimLabTest)
            hygrometer.registerHygrometerListener(this@PICSimLabTest)
            hygrometer.start(Duration.ofSeconds(15))

            delay(120000)

            board.stop()
        }
    }

    override fun onStart(event: IOEvent) {
        println("start: $event")
    }

    override fun onStop(event: IOEvent) {
        println("stop: $event")
    }

    override fun onPinChange(event: IOEvent) {
        // println("pinChange: ${event.pin}")
    }

    override fun onMessageReceive(event: IOEvent, message: String) {
        println("$event: $message")
    }

    override fun onTemperatureChange(thermometer: Thermometer) {
        println("${thermometer::class.simpleName}: ${thermometer.temperature} °C")
    }

    override fun onHumidityChange(hygrometer: Hygrometer) {
        println("${hygrometer::class.simpleName}: ${hygrometer.humidity} %")
    }
}
