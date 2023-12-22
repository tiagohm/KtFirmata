package kt.firmata.core.protocol.transport

import kt.firmata.core.protocol.parser.Parser
import java.io.Closeable

interface Transport : Closeable {

    val parser: Parser

    fun start()

    fun write(b: Int)

    fun write(source: ByteArray, offset: Int = 0, byteCount: Int = source.size)

    fun flush()
}
