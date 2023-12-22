package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.ErrorEvent
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.fsm.SystemResetEvent
import kt.firmata.core.protocol.parser.FirmataToken.ANALOG_MESSAGE
import kt.firmata.core.protocol.parser.FirmataToken.DIGITAL_MESSAGE
import kt.firmata.core.protocol.parser.FirmataToken.REPORT_VERSION
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.SYSTEM_RESET

data class WaitingForMessageState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    override fun process(b: Int) {
        // First byte may contain not only command but additional information as well.
        val command = if (b < 0xF0) (b and 0xF0) else b

        when (command) {
            DIGITAL_MESSAGE -> transitTo(ParsingDigitalMessageState(finiteStateMashine, b and 0x0F))
            ANALOG_MESSAGE -> transitTo(ParsingAnalogMessageState(finiteStateMashine, b and 0x0F))
            REPORT_VERSION -> transitTo<ParsingVersionMessageState>()
            START_SYSEX -> transitTo<ParsingSysexMessageState>()
            SYSTEM_RESET -> publish(SystemResetEvent)
            // Skip non control token.
            else -> publish(ErrorEvent(command))
        }
    }
}
