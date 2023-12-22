package kt.firmata.core.protocol.fsm

data class VersionMessageEvent(val major: Int, val minor: Int) : Event
