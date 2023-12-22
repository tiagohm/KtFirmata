package kt.firmata.core.protocol.fsm

data class FiniteStateMachineInTerminalStateEvent(val finiteStateMachine: FiniteStateMachine) : Event
