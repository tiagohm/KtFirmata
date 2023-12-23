package kt.firmata.core.protocol.message

import kt.firmata.core.Pin
import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.parser.FirmataToken.REPORT_DIGITAL
import kt.firmata.core.protocol.transport.Transport

data class ReportDigitalPin(val pin: Pin, val enabled: Boolean) : FirmataMessage {

    override fun sendTo(board: Board, transport: Transport) {
        if (board.isPinDigital(pin)) {
            transport.write(REPORT_DIGITAL or pin.index)
            transport.write(if (enabled) 1 else 0)
        }
    }
}
