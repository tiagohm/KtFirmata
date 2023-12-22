package kt.firmata.core.protocol.fsm

data class I2CMessageEvent(val address: Int, val register: Int, val message: ByteArray) : Event
