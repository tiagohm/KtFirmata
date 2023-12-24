package kt.firmata.server

import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import jakarta.annotation.PostConstruct
import kt.firmata.hardware.*
import kt.firmata.server.handler.incoming.IncomingDataHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.Duration
import java.util.*
import java.util.function.Consumer

@Service
class Server(
    @Value("\${server.id}") private val id: String,
    @Autowired private val client: Mqtt3AsyncClient,
    @Autowired private val hardwares: List<Hardware>,
    @Autowired private val incomingDataHandlers: List<IncomingDataHandler>,
) : Runnable, Consumer<Mqtt3Publish>,
    ThermometerListener<Thermometer<*>>, HygrometerListener<Hygrometer<*>>,
    BarometerListener<Barometer<*>>, AltimeterListener<Altimeter<*>> {

    @PostConstruct
    override fun run() {
        for (hardware in hardwares) {
            register(hardware)
        }

        subscribe("monitor")
        subscribe("pinWrite")

        client.publishes(MqttGlobalPublishFilter.ALL, this)
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

        hardware.start(POOLING_TIME)
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

    override fun accept(message: Mqtt3Publish) {
        val topic = message.topic.toString()
        val data = Charsets.UTF_8.decode(message.payload.get()).toString()

        for (handler in incomingDataHandlers) {
            if (handler.handle(topic, data)) {
                break
            }
        }
    }

    override fun onTemperatureChange(thermometer: Thermometer<*>) {
        publish("${thermometer.name}/temperature", "%.01f".format(Locale.ENGLISH, thermometer.temperature))
    }

    override fun onHumidityChange(hygrometer: Hygrometer<*>) {
        publish("${hygrometer.name}/humidity", "%.01f".format(Locale.ENGLISH, hygrometer.humidity))
    }

    override fun onPressureChange(barometer: Barometer<*>) {
        publish("${barometer.name}/pressure", "%.01f".format(Locale.ENGLISH, barometer.pressure))
    }

    override fun onAltitudeChange(altimeter: Altimeter<*>) {
        publish("${altimeter.name}/altitude", "%.01f".format(Locale.ENGLISH, altimeter.altitude))
    }

    fun subscribe(topic: String) {
        client.subscribeWith()
            .topicFilter("$id/$topic")
            .send()
            .get()
    }

    fun publish(topic: String, payload: ByteBuffer) {
        client.publishWith()
            .topic("$id/$topic")
            .payload(payload)
            .send()
    }

    fun publish(topic: String, payload: ByteArray, offset: Int = 0, byteCount: Int = payload.size) {
        publish(topic, ByteBuffer.wrap(payload, offset, byteCount))
    }

    fun publish(topic: String, payload: String, charset: Charset = Charsets.UTF_8) {
        publish(topic, charset.encode(payload))
    }

    companion object {

        @JvmStatic private val POOLING_TIME = Duration.ofSeconds(30)
        @JvmStatic private val LOG = LoggerFactory.getLogger(Server::class.java)
    }
}
