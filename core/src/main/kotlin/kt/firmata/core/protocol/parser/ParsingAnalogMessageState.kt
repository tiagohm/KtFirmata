package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.AnalogMessageEvent
import kt.firmata.core.protocol.fsm.FiniteStateMachine

data class ParsingAnalogMessageState(override val finiteStateMashine: FiniteStateMachine, private val portId: Int) : AbstractState() {

    @Volatile private var counter = 0
    @Volatile private var value = 0

    override fun process(b: Int) {
        when (counter) {
            0 -> {
                value = b
                counter++
            }
            1 -> {
                value = value or (b shl 7)

                publish(AnalogMessageEvent(portId, value))
                transitTo<WaitingForMessageState>()
            }
        }
    }
}
