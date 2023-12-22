package kt.firmata.core.protocol

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

internal data class DaemonThreadFactory(private val prefix: String) : ThreadFactory {

    private val counter = AtomicInteger()

    override fun newThread(action: Runnable): Thread {
        val thread = Thread(action, "%s-%d".format(prefix, counter.incrementAndGet()))
        thread.isDaemon = true
        return thread
    }
}
