package kt.firmata.core.protocol.fsm

import kt.firmata.core.PinMode

data class PinStateEvent(val pinId: Int, val mode: PinMode, val value: Int) : Event
