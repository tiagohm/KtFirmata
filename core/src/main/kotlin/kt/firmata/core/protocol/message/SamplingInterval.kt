package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.MAX_SAMPLING_INTERVAL
import kt.firmata.core.protocol.parser.FirmataToken.MIN_SAMPLING_INTERVAL
import kt.firmata.core.protocol.parser.FirmataToken.SAMPLING_INTERVAL
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport
import kotlin.math.max
import kotlin.math.min

data class SamplingInterval(val interval: Int) : FirmataMessage {

    override fun sendTo(board: Board, transport: Transport) {
        val value = max(MIN_SAMPLING_INTERVAL, min(interval, MAX_SAMPLING_INTERVAL))

        transport.write(START_SYSEX)
        transport.write(SAMPLING_INTERVAL)
        transport.write(value and 0x7F)
        transport.write(value ushr 7 and 0x7F)
        transport.write(END_SYSEX)
    }
}
