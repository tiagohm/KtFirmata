package kt.firmata.core.protocol.fsm

import kt.firmata.core.PinMode

data class PinCapabilityResponseEvent(val pinId: Int, val supportedModes: Set<PinMode>) : Event
