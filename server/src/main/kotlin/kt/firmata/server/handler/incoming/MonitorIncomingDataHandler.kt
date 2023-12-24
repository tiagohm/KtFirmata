package kt.firmata.server.handler.incoming

import kt.firmata.hardware.Hardware
import kt.firmata.hardware.HardwareScheduler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MonitorIncomingDataHandler(
    @Value("\${server.id}") private val id: String,
    @Autowired private val hardwares: List<Hardware>,
) : IncomingDataHandler {

    private val regex = Regex("$id/monitor")

    override fun handle(topic: String, data: String): Boolean {
        regex.matchEntire(topic) ?: return false

        for (hardware in hardwares) {
            if (hardware is Runnable) {
                HardwareScheduler.submit(hardware)
            }
        }

        return true
    }
}
