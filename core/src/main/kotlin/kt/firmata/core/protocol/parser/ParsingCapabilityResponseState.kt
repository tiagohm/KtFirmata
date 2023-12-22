package kt.firmata.core.protocol.parser

import kt.firmata.core.PinMode
import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.fsm.PinCapabilitiesFinishedEvent
import kt.firmata.core.protocol.fsm.PinCapabilityResponseEvent
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX

data class ParsingCapabilityResponseState @JvmOverloads constructor(
    override val finiteStateMashine: FiniteStateMachine,
    private val pinId: Int = 0,
) : AbstractState() {

    override fun process(b: Int) {
        when (b) {
            END_SYSEX -> {
                publish(PinCapabilitiesFinishedEvent)
                transitTo<WaitingForMessageState>()
            }
            127 -> {
                val supportedModes = HashSet<PinMode>(count / 2)
                var i = 0

                while (i < count) {
                    // Every second byte contains mode's resolution of pin.
                    supportedModes.add(PinMode.resolve(buf[i].toInt()))
                    i += 2
                }

                publish(PinCapabilityResponseEvent(pinId, supportedModes))
                transitTo(ParsingCapabilityResponseState(finiteStateMashine, pinId + 1))
            }
            else -> {
                write(b)
            }
        }
    }
}
