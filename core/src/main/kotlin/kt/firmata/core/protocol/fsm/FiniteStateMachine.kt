package kt.firmata.core.protocol.fsm

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.function.Consumer

class FiniteStateMachine(type: Class<out State>) : State {

    private val handlers = ConcurrentHashMap<Class<*>, Consumer<in Event>>()

    @Volatile var eventHandlingExecutor: Executor = DirectExecutor

    @Volatile var currentState: State? = type.getConstructor(FiniteStateMachine::class.java).newInstance(this)
        private set

    override val finiteStateMashine
        get() = this

    init {
        handlers[Any::class.java] = Consumer<Event> {}
    }

    fun transitTo(state: State) {
        currentState = state
    }

    fun transitTo(type: Class<out State>) {
        try {
            transitTo(type.getConstructor(FiniteStateMachine::class.java).newInstance(this))
        } catch (ex: ReflectiveOperationException) {
            throw IllegalArgumentException("Cannot instantiate the new state from type: $type", ex)
        }
    }

    override fun process(b: Int) {
        if (currentState == null) {
            handle(FiniteStateMachineInTerminalStateEvent(this))
        } else {
            currentState!!.process(b)
        }
    }

    @JvmOverloads
    fun process(buffer: ByteArray, offset: Int = 0, length: Int = buffer.size) {
        for (i in offset until offset + length) {
            process(buffer[i].toInt() and 0xFF)
        }
    }

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <T : Event> addHandler(type: Class<out T>, handler: Consumer<in T>) {
        if (handlers.containsKey(type)) {
            handlers[type] = handlers[type]!!.andThen(handler as Consumer<Any?>)
        } else {
            handlers[type] = handler as Consumer<in Event>
        }
    }

    inline fun <reified T : Event> addHandler(handler: Consumer<in T>) {
        addHandler(T::class.java, handler)
    }

    internal fun handle(event: Event) {
        val handler = handlers[event::class.java]

        if (handler == null) {
            LOG.warn("No specific event handler is registered for {}", event)
        } else {
            eventHandlingExecutor.execute { handler.accept(event) }
        }

        eventHandlingExecutor.execute { handlers[Any::class.java]!!.accept(event) }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(FiniteStateMachine::class.java)
    }
}
