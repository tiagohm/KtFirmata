package kt.firmata.core.protocol.transport

import java.io.ByteArrayOutputStream

abstract class ByteArrayOutputStreamTransport : Transport, ByteArrayOutputStream() {

    protected abstract fun flushBytes(bytes: ByteArray, size: Int)

    final override fun flush() {
        flushBytes(buf, count)
        reset()
    }
}
