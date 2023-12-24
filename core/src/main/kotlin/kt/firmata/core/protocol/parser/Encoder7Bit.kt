package kt.firmata.core.protocol.parser

object Encoder7Bit {

    @JvmStatic
    fun decode(data: ByteArray, offset: Int): Int {
        return (data[offset + 1].toInt() and 0x01 shl 7) or (data[offset].toInt() and 0x7F)
    }
}
