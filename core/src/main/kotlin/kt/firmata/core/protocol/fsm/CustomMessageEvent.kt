package kt.firmata.core.protocol.fsm

import java.nio.ByteBuffer

data class CustomMessageEvent(val message: ByteBuffer) : Event
