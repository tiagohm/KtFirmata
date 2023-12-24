package kt.firmata.server

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class MQTTConfig {

    @Value("\${MQTT_HOST}") private lateinit var host: String
    @Value("\${MQTT_PORT:8883}") private var port = 8883
    @Value("\${MQTT_USERNAME}") private lateinit var username: String
    @Value("\${MQTT_PASSWORD}") private lateinit var password: String

    @Bean
    fun client(): Mqtt3AsyncClient {
        val clientBuilder = MqttClient.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost(host)
            .serverPort(port)
            .sslWithDefaultConfig()

        val client = clientBuilder.useMqttVersion3().buildAsync()

        client.connectWith()
            .simpleAuth()
            .username(username)
            .password(Charsets.UTF_8.encode(password))
            .applySimpleAuth()
            .send()
            .get()

        return client
    }
}
