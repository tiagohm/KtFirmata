package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.parser.FirmataToken.ANALOG_MAPPING_QUERY
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

data object RequestAnalogMapping : FirmataMessage {

    override fun sendTo(board: Board, transport: Transport) {
        transport.write(START_SYSEX)
        transport.write(ANALOG_MAPPING_QUERY)
        transport.write(END_SYSEX)
    }
}
