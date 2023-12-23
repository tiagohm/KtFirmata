package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.STRING_DATA
import kt.firmata.core.protocol.transport.Transport

data class Text(val message: String) : FirmataMessage {

    override fun sendTo(board: Board, transport: Transport) {
        transport.write(START_SYSEX)
        transport.write(STRING_DATA)

        val bytes = message.toByteArray()

        for (i in bytes.indices) {
            val b = bytes[i].toInt()
            transport.write(b and 0x7F)
            transport.write(b shr 7 and 0x7F)
        }

        transport.write(END_SYSEX)
    }
}
