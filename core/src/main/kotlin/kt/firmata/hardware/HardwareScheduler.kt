package kt.firmata.hardware

import kt.firmata.core.protocol.DaemonThreadFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

internal object HardwareScheduler :
    ScheduledExecutorService by Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors(),
        DaemonThreadFactory("Firmata-Hardware-Scheduler")
    )
