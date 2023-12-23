package kt.firmata.core.protocol.message

import kt.firmata.core.Pin
import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.parser.FirmataToken.REPORT_ANALOG
import kt.firmata.core.protocol.transport.Transport

data class ReportAnalogPin(val pin: Pin, val enabled: Boolean) : FirmataMessage {

    override fun sendTo(board: Board, transport: Transport) {
        if (board.isPinAnalog(pin)) {
            transport.write(REPORT_ANALOG or board.pinToAnalogIndex(pin))
            transport.write(if (enabled) 1 else 0)
        }
    }
}
