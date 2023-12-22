package kt.firmata.core.protocol.fsm

data class AnalogMappingEvent(val mapping: Map<Int, Int>) : Event
