package kt.firmata.core.protocol.fsm

data class ErrorEvent(val command: Int) : Exception("Unknown control token has been received. Skipping. 0x%2x".format(command)), Event
