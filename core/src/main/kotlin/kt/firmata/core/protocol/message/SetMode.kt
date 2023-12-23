package kt.firmata.core.protocol.message

import kt.firmata.core.PinMode
import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.parser.FirmataToken.SET_PIN_MODE
import kt.firmata.core.protocol.transport.Transport

data class SetMode(val pinId: Int, val mode: PinMode) : FirmataMessage {

    init {
        require(mode !== PinMode.UNSUPPORTED) { "Cannot set unsupported mode to pin $pinId" }
    }

    override fun sendTo(board: Board, transport: Transport) {
        transport.write(SET_PIN_MODE)
        transport.write(pinId)
        transport.write(mode.ordinal)
    }
}
