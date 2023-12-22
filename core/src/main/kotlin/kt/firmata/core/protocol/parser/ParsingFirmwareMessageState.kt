package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.fsm.FirmwareMessageEvent
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX

data class ParsingFirmwareMessageState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    override fun process(b: Int) {
        if (b == END_SYSEX) {
            val major = buf[0].toInt()
            val minor = buf[1].toInt()
            val name = ParsingStringMessageState.decode(buf, 2, count - 2)
            publish(FirmwareMessageEvent(major, minor, name))
            transitTo<WaitingForMessageState>()
        } else {
            write(b)
        }
    }
}
