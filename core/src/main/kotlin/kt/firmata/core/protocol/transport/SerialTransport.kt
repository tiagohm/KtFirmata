package kt.firmata.core.protocol.transport

class SerialTransport(
    portName: String,
    baudRate: Int = 57600, dataBits: Int = 8,
    stopBits: Int = 1, parity: Int = 0,
) : Transport {

    private val delegate: Transport

    override var parser
        get() = delegate.parser
        set(value) {
            delegate.parser = value
        }

    init {
        try {
            Class.forName("com.fazecast.jSerialComm.SerialPort", false, javaClass.classLoader)
            delegate = JSerialCommTransport(portName, baudRate, dataBits, stopBits, parity)
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

    override fun toString(): String {
        return delegate.toString()
    }
}
