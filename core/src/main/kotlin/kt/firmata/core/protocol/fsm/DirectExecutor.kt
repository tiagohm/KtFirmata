package kt.firmata.core.protocol.fsm

import java.util.concurrent.Executor

data object DirectExecutor : Executor {

    override fun execute(command: Runnable) {
        command.run()
    }
}
