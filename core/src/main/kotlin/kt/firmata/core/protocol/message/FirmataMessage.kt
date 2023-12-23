package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.transport.Transport

fun interface FirmataMessage {

    fun sendTo(board: Board, transport: Transport)
}
