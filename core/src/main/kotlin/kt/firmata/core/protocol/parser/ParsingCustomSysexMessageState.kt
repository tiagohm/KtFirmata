package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.CustomMessageEvent
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX

data class ParsingCustomSysexMessageState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    override fun process(b: Int) {
        if (b == END_SYSEX) {
            publish(CustomMessageEvent(toByteArray()))
            transitTo<WaitingForMessageState>()
        } else {
            write(b)
        }
    }
}
