package kt.firmata.server.handler.incoming

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PinWriteIncomingDataHandler(
    @Value("\${server.id}") private val id: String,
) : IncomingDataHandler {

    // TODO: Expansion port.
    private val regex = Regex("$id/pinWrite")

    override fun handle(topic: String, data: String): Boolean {
        regex.matchEntire(topic) ?: return false

        val id = data[0].digitToInt()
        val value = data[1].digitToInt() == 1

        return true
    }
}
