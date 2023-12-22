package kt.firmata.core.protocol.transport

import kt.firmata.core.protocol.parser.Parser

class SerialTransport(portName: String, override val parser: Parser) : Transport {

    private val delegate: Transport

    init {
        try {
            Class.forName("com.fazecast.jSerialComm.SerialPort", false, javaClass.classLoader)
            delegate = JSerialCommTransport(portName, parser)
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException(
                "Serial communication library is not found in the classpath. "
                        + "Please make sure that there is at least one dependency "
                        + "as described in the javadoc of org.firmata4j.transport.SerialTransport"
            )
        }
    }

    override fun start() {
        delegate.start()
    }

    override fun write(b: Int) {
        delegate.write(b)
    }

    override fun write(source: ByteArray, offset: Int, byteCount: Int) {
        delegate.write(source, offset, byteCount)
    }

    override fun flush() {
        delegate.flush()
    }

    override fun close() {
        delegate.close()
    }
}
