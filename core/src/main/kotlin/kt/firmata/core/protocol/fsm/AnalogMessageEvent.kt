package kt.firmata.core.protocol.fsm

data class AnalogMessageEvent(val pinId: Int, val value: Int) : Event
