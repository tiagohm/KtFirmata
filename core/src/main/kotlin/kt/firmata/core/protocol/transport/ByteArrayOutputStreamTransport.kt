package kt.firmata.core.protocol.transport

import java.io.ByteArrayOutputStream

abstract class ByteArrayOutputStreamTransport(size: Int = 32) : Transport, ByteArrayOutputStream(size) {

    protected abstract fun flushBytes(bytes: ByteArray, size: Int)

    @Synchronized
    final override fun flush() {
        flushBytes(buf, count)
        reset()
    }
}
