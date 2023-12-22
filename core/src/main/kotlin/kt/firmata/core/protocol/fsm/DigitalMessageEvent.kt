package kt.firmata.core.protocol.fsm

data class DigitalMessageEvent(val pinId: Int, val value: Int) : Event
