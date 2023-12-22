package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.AnalogMessageEvent
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX

data class ParsingExtendedAnalogMessageState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    override fun process(b: Int) {
        if (b == END_SYSEX) {
            val pinId = buf[0].toInt()
            var value = buf[1].toInt()

            for (i in 2 until count) {
                value = value or (buf[i].toInt() shl 7 * (i - 1))
            }

            publish(AnalogMessageEvent(pinId, value))
            transitTo<WaitingForMessageState>()
        } else {
            write(b)
        }
    }
}
