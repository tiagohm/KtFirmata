package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.transport.Transport

fun interface FirmataMessage {

    fun sendTo(transport: Transport)
}
