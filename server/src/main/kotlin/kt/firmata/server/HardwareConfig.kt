package kt.firmata.server

import com.fazecast.jSerialComm.SerialPort
import kt.firmata.core.IODevice
import kt.firmata.core.protocol.board.ArduinoUno
import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.transport.SerialTransport
import kt.firmata.hardware.AM2320
import kt.firmata.hardware.LM35
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HardwareConfig {

    @Bean
    fun board(): IODevice {
        for (port in SerialPort.getCommPorts()) {
            LOG.info("Connecting to board at port: {}", port)

            val transport = SerialTransport(port.systemPortName, 115200)
            val board = ArduinoUno(transport)
            board.start()

            try {
                board.ensureInitializationIsDone()
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
    fun am2320(board: Board): AM2320 {
        return AM2320(board)
    }

    @Bean
    fun lm35(board: Board): LM35 {
        return LM35(board, board.pinAt(ArduinoUno.A0))
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(HardwareConfig::class.java)
    }
}
