package kt.firmata.core.protocol.fsm

data class PinStateEvent(val pinId: Int, val mode: Int, val value: Int) : Event
