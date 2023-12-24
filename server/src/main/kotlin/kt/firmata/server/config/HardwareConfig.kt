package kt.firmata.server.config

import com.fazecast.jSerialComm.SerialPort
import kt.firmata.core.IODevice
import kt.firmata.core.protocol.board.ArduinoUno
import kt.firmata.core.protocol.transport.SerialTransport
import kt.firmata.hardware.AM2320
import kt.firmata.hardware.BMP180
import kt.firmata.hardware.LM35
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Path
import java.util.*
import kotlin.io.path.outputStream

@Configuration
class HardwareConfig {

    @Bean
    fun board(propertiesPath: Path, properties: Properties): IODevice {
        val usedSystemPortName = properties.getProperty("usedSystemPortName", "")
        val availablePorts = SerialPort.getCommPorts()
        val usedPort = availablePorts.indexOfFirst { it.systemPortName == usedSystemPortName }

        if (usedPort > 0) {
            val a = availablePorts[0]
            availablePorts[0] = availablePorts[usedPort]
            availablePorts[usedPort] = a
        }

        for (port in availablePorts) {
            LOG.info("Connecting to board at port: {}", port)

            val transport = SerialTransport(port.systemPortName, 115200)
            val board = ArduinoUno(transport)
            board.start()

            try {
                board.ensureInitializationIsDone()
                properties.setProperty("usedSystemPortName", port.systemPortName)
                propertiesPath.outputStream().use { properties.store(it, "KtFirmataServer") }
                return board
            } catch (e: InterruptedException) {
                transport.close()
                LOG.error("Failed to connect board at port: {}", port)
                continue
            }
        }

        throw IllegalStateException("A board cannot be found")
    }

    @Bean
    fun lm35(board: IODevice): LM35 {
        return LM35(board, board.pinAt(ArduinoUno.A0))
    }

    @Bean
    fun am2320(board: IODevice): AM2320 {
        return AM2320(board)
    }

    @Bean
    fun bmp180(board: IODevice): BMP180 {
        return BMP180(board)
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(HardwareConfig::class.java)
    }
}
