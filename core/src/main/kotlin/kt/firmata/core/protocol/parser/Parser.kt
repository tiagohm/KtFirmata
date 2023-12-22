package kt.firmata.core.protocol.parser

interface Parser {

    fun start()

    fun stop()

    fun parse(bytes: ByteArray)
}
