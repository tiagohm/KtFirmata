package kt.firmata.core.protocol.fsm

interface State {

    fun process(b: Int)

    val finiteStateMashine: FiniteStateMachine
}
