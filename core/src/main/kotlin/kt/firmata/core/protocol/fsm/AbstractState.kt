package kt.firmata.core.protocol.fsm

import java.io.ByteArrayOutputStream

abstract class AbstractState : ByteArrayOutputStream(), State {

    protected fun transitTo(type: Class<out State>) {
        finiteStateMashine.transitTo(type)
    }

    protected inline fun <reified T : State> transitTo() {
        finiteStateMashine.transitTo(T::class.java)
    }

    protected fun transitTo(state: State) {
        finiteStateMashine.transitTo(state)
    }

    protected fun publish(event: Event) {
        finiteStateMashine.handle(event)
    }
}
