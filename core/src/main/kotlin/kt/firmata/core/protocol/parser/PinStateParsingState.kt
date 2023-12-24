package kt.firmata.core.protocol.parser

import kt.firmata.core.PinMode
import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.fsm.PinStateEvent
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX

data class PinStateParsingState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    override fun process(b: Int) {
        if (b == END_SYSEX) {
            var value = 0

            for (i in 2 until count) {
                value = value or (buf[i].toInt() shl ((i - 2) * 7))
            }

            publish(PinStateEvent(buf[0].toInt(), PinMode.resolve(buf[1].toInt()), value))
            transitTo<WaitingForMessageState>()
        } else {
            write(b)
        }
    }
}
