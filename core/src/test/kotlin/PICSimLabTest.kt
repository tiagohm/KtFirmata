import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import kt.firmata.core.IODeviceEventListener
import kt.firmata.core.IOEvent
import kt.firmata.core.protocol.FirmataDevice
import kt.firmata.core.protocol.transport.SerialTransport

class PICSimLabTest : StringSpec(), IODeviceEventListener {

    init {
        "report" {
            val transport = SerialTransport("COM4")
            val device = FirmataDevice(transport)

            device.addEventListener(this@PICSimLabTest)

            device.start()
            device.ensureInitializationIsDone()

            for (pin in device.pins) {
                println(pin)
            }

            delay(120000)

            device.stop()
        }
    }

    override fun onStart(event: IOEvent) {
        println("start: $event")
    }

    override fun onStop(event: IOEvent) {
        println("stop: $event")
    }

    override fun onPinChange(event: IOEvent) {
        println("pinChange: ${event.pin}")
    }

    override fun onMessageReceive(event: IOEvent, message: String) {
        println("$event: $message")
    }
}
