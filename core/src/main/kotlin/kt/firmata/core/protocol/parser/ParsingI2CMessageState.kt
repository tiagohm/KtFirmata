package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.fsm.I2CMessageEvent
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX

data class ParsingI2CMessageState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    override fun process(b: Int) {
        if (b == END_SYSEX) {
            val (address, register, message) = buf.parse()
            publish(I2CMessageEvent(address, register, message))
            transitTo<WaitingForMessageState>()
        } else {
            write(b)
        }
    }

    companion object {

        @JvmStatic
        private fun ByteArray.parse(): Triple<Int, Int, IntArray> {
            val address = Encoder7Bit.decode(this, 0)
            val register = Encoder7Bit.decode(this, 2)
            val data = IntArray((size - 4) / 2) { Encoder7Bit.decode(this, it * 2 + 4) }

            return Triple(address, register, data)
        }
    }
}
