package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.fsm.VersionMessageEvent

data class ParsingVersionMessageState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    @Volatile private var counter = 0
    @Volatile private var major = 0

    override fun process(b: Int) {
        if (counter == 0) {
            major = b
            counter++
        } else {
            publish(VersionMessageEvent(major, b))
            transitTo<WaitingForMessageState>()
        }
    }
}
