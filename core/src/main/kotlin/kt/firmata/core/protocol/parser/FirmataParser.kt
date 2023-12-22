package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.FiniteStateMachine
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

data class FirmataParser(private val fsm: FiniteStateMachine) : Parser, Runnable {

    @Volatile private var parserExecutor: Thread? = null

    private val byteQueue = LinkedBlockingQueue<ByteArray>()
    private val running = AtomicBoolean()

    override fun start() {
        if (!running.getAndSet(true)) {
            parserExecutor = Thread(this, "Firmata-Parser-Thread")
            parserExecutor!!.isDaemon = true
            parserExecutor!!.start()
        }
    }

    override fun stop() {
        if (running.getAndSet(false)) {
            byteQueue.clear()

            // Interrupt the thread to ensure it falls out of the loop
            // and sees the shutdown request.
            parserExecutor!!.interrupt()

            try {
                parserExecutor!!.join(WAIT_FOR_TERMINATION_DELAY)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                LOG.warn("Parser didn't stop gracefully")
            }
        }
    }

    override fun parse(bytes: ByteArray) {
        if (!byteQueue.offer(bytes)) {
            LOG.warn("Parser reached byte queue limit. Some bytes were skipped.")
        }
    }

    override fun run() {
        while (running.get()) {
            try {
                fsm.process(byteQueue.take())
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    companion object {

        private const val WAIT_FOR_TERMINATION_DELAY = 3000L

        @JvmStatic private val LOG = LoggerFactory.getLogger(FirmataParser::class.java)
    }
}
