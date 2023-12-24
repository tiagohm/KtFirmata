package kt.firmata.core.protocol.message

import kt.firmata.core.IODevice
import kt.firmata.core.protocol.transport.Transport

fun interface FirmataMessage {

    fun sendTo(board: IODevice, transport: Transport)
}
