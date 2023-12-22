package kt.firmata.core.protocol.transport

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import kt.firmata.core.protocol.parser.Parser
import java.io.IOException

class JSerialCommTransport(portDescriptor: String, override val parser: Parser) : ByteArrayOutputStreamTransport() {

    private val serialPort = SerialPort.getCommPort(portDescriptor)

    override fun start() {
        if (!serialPort.isOpen) {
            if (serialPort.openPort()) {
                serialPort.setComPortParameters(57600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY)
                serialPort.addDataListener(object : SerialPortDataListener {

                    override fun serialEvent(event: SerialPortEvent) {
                        if (event.eventType == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
                            parser.parse(event.receivedData)
                        }
                    }

                    override fun getListeningEvents(): Int {
                        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
                    }
                })
            } else {
                throw IOException("Cannot start firmata device: port=$serialPort")
            }
        }
    }

    override fun close() {
        if (serialPort.isOpen && !serialPort.closePort()) {
            throw IOException("Cannot properly stop firmata device. port=$serialPort")
        }
    }

    override fun flushBytes(bytes: ByteArray, size: Int) {
        serialPort.writeBytes(bytes, size)
    }
}
