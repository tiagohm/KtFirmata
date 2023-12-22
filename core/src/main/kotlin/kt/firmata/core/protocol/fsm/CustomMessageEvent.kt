package kt.firmata.core.protocol.fsm

data class CustomMessageEvent(val message: ByteArray) : Event
