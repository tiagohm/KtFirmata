package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.fsm.StringMessageEvent
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX

data class ParsingStringMessageState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    override fun process(b: Int) {
        if (b == END_SYSEX) {
            val value = decode(buf, 0, count)
            publish(StringMessageEvent(value))
            transitTo<WaitingForMessageState>()
        } else {
            write(b)
        }
    }

    companion object {

        @JvmStatic
        fun decode(buffer: ByteArray, offset: Int = 0, length: Int = buffer.size): String {
            val decoded = CharArray(length / 2)
            var k = 0

            for (i in offset until offset + length step 2) {
                decoded[k++] = (buffer[i] + (buffer[i + 1].toInt() shl 7)).toChar()
            }

            return decoded.concatToString()
        }
    }
}
