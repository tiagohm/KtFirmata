package kt.firmata.core.protocol.transport

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import kt.firmata.core.protocol.parser.Parser
import java.io.IOException

class JSerialCommTransport(
    portDescriptor: String,
    private val baudRate: Int = 57600, private val dataBits: Int = 8,
    private val stopBits: Int = SerialPort.ONE_STOP_BIT, private val parity: Int = SerialPort.NO_PARITY,
) : ByteArrayOutputStreamTransport() {

    private val serialPort = SerialPort.getCommPort(portDescriptor)

    override lateinit var parser: Parser

    override fun start() {
        if (!serialPort.isOpen) {
            if (serialPort.openPort()) {
                serialPort.setComPortParameters(baudRate, dataBits, stopBits, parity)
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

    override fun toString(): String {
        return "JSerialCommTransport(baudRate=$baudRate, dataBits=$dataBits, stopBits=$stopBits," +
                " parity=$parity, serialPort=${serialPort.portDescription})"
    }
}
