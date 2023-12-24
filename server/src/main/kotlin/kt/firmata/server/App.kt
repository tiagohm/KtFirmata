package kt.firmata.server

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import kt.firmata.core.IODevice
import kt.firmata.hardware.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Lazy
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.Duration
import java.util.*

@SpringBootApplication
class App : CommandLineRunner, ThermometerListener<Thermometer<*>>, HygrometerListener<Hygrometer<*>> {

    @Value("\${server.id}") private lateinit var id: String

    @Autowired @Lazy private lateinit var board: IODevice
    @Autowired @Lazy private lateinit var client: Mqtt3AsyncClient
    @Autowired @Lazy private lateinit var hardwares: List<Hardware>

    override fun run(vararg args: String) {
        for (hardware in hardwares) {
            register(hardware)
        }
    }

    fun register(hardware: Hardware) {
        if (hardware is Thermometer<*>) {
            hardware.registerThermometerListener(this)
        }
        if (hardware is Hygrometer<*>) {
            hardware.registerHygrometerListener(this)
        }

        hardware.start(INTERVAL)
    }

    fun unregister(hardware: Hardware) {
        hardware.stop()

        if (hardware is Thermometer<*>) {
            hardware.unregisterThermometerListener(this)
        }
        if (hardware is Hygrometer<*>) {
            hardware.unregisterHygrometerListener(this)
        }
    }

    override fun onTemperatureChange(thermometer: Thermometer<*>) {
        send("$id/${thermometer.name}/temperature", "%.01f".format(Locale.ENGLISH, thermometer.temperature))
    }

    override fun onHumidityChange(hygrometer: Hygrometer<*>) {
        send("$id/${hygrometer.name}/humidity", "%.01f".format(Locale.ENGLISH, hygrometer.humidity))
    }

    private fun send(topic: String, payload: ByteBuffer) {
        client.publishWith()
            .topic(topic)
            .payload(payload)
            .send()
    }

    private fun send(topic: String, payload: ByteArray, offset: Int = 0, byteCount: Int = payload.size) {
        send(topic, ByteBuffer.wrap(payload, offset, byteCount))
    }

    private fun send(topic: String, payload: String, charset: Charset = Charsets.UTF_8) {
        send(topic, charset.encode(payload))
    }

    companion object {

        @JvmStatic private val INTERVAL = Duration.ofSeconds(30)
    }
}
