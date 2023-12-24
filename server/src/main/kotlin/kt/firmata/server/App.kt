package kt.firmata.server

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import kt.firmata.core.IODevice
import kt.firmata.hardware.*
import org.slf4j.LoggerFactory
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
class App : CommandLineRunner, ThermometerListener<Thermometer<*>>, HygrometerListener<Hygrometer<*>>,
    BarometerListener<Barometer<*>>, AltimeterListener<Altimeter<*>> {

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
            LOG.info("{} was registered as thermometer", hardware.name)
            hardware.registerThermometerListener(this)
        }
        if (hardware is Hygrometer<*>) {
            LOG.info("{} was registered as hygrometer", hardware.name)
            hardware.registerHygrometerListener(this)
        }
        if (hardware is Barometer<*>) {
            LOG.info("{} was registered as barometer", hardware.name)
            hardware.registerBarometerListener(this)
        }
        if (hardware is Altimeter<*>) {
            LOG.info("{} was registered as altimeter", hardware.name)
            hardware.registerAltimeterListener(this)
        }

        hardware.start(INTERVAL)
    }

    fun unregister(hardware: Hardware) {
        hardware.close()

        if (hardware is Thermometer<*>) {
            hardware.unregisterThermometerListener(this)
        }
        if (hardware is Hygrometer<*>) {
            hardware.unregisterHygrometerListener(this)
        }
        if (hardware is Barometer<*>) {
            hardware.unregisterBarometerListener(this)
        }
        if (hardware is Altimeter<*>) {
            hardware.unregisterAltimeterListener(this)
        }
    }

    override fun onTemperatureChange(thermometer: Thermometer<*>) {
        send("$id/${thermometer.name}/temperature", "%.01f".format(Locale.ENGLISH, thermometer.temperature))
    }

    override fun onHumidityChange(hygrometer: Hygrometer<*>) {
        send("$id/${hygrometer.name}/humidity", "%.01f".format(Locale.ENGLISH, hygrometer.humidity))
    }

    override fun onPressureChange(barometer: Barometer<*>) {
        send("$id/${barometer.name}/pressure", "%.01f".format(Locale.ENGLISH, barometer.pressure))
    }

    override fun onAltitudeChange(altimeter: Altimeter<*>) {
        send("$id/${altimeter.name}/altitude", "%.03f".format(Locale.ENGLISH, altimeter.altitude))
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
        @JvmStatic private val LOG = LoggerFactory.getLogger(App::class.java)
    }
}
