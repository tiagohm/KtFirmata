package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.DigitalMessageEvent
import kt.firmata.core.protocol.fsm.FiniteStateMachine

data class ParsingDigitalMessageState(override val finiteStateMashine: FiniteStateMachine, private val portId: Int) : AbstractState() {

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
                val pinId = portId * 8

                repeat(8) {
                    publish(DigitalMessageEvent(pinId + it, value ushr it and 0x01))
                }

                transitTo<WaitingForMessageState>()
            }
        }
    }
}
