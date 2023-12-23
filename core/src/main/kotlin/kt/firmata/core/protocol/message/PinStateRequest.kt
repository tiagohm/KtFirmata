package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.PIN_STATE_QUERY
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

data class PinStateRequest(val pinId: Int) : FirmataMessage {

    override fun sendTo(board: Board, transport: Transport) {
        transport.write(START_SYSEX)
        transport.write(PIN_STATE_QUERY)
        transport.write(pinId)
        transport.write(END_SYSEX)
    }
}
