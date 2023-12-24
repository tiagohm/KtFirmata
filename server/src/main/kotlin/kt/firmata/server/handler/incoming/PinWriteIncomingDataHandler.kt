package kt.firmata.server.handler.incoming

import kt.firmata.hardware.DigitalOutput
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PinWriteIncomingDataHandler(
    @Value("\${server.id}") private val id: String,
    @Qualifier("outputs") private val outputs: List<DigitalOutput<*>>,
) : IncomingDataHandler {

    // TODO: Expansion port.
    private val regex = Regex("$id/(D\\d+)/pinWrite")

    override val topic = "+/pinWrite"

    override fun handle(topic: String, data: String): Boolean {
        val m = regex.matchEntire(topic) ?: return false

        val name = m.groupValues[1]
        val pin = outputs.find { it.name == name } ?: return false

        if (data == "1") pin.on() else pin.off()

        return true
    }
}
