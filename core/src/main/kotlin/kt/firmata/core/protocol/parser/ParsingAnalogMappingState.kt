package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.AnalogMappingEvent
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import java.util.concurrent.ConcurrentHashMap

data class ParsingAnalogMappingState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    private val mapping = ConcurrentHashMap<Int, Int>()

    @Volatile private var portId = 0

    override fun process(b: Int) {
        if (b == END_SYSEX) {
            publish(AnalogMappingEvent(mapping))
            transitTo<WaitingForMessageState>()
        } else if (b != 127) {
            // If pin does support analog, corresponding analog id is in the byte.
            mapping[b] = portId
        }

        portId++
    }
}
