package kt.firmata.core.protocol.fsm

data class FirmwareMessageEvent(val major: Int, val minor: Int, val message: String) : Event
