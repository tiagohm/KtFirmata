package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.parser.FirmataToken.DIGITAL_MESSAGE
import kt.firmata.core.protocol.transport.Transport

data class SetDigitalPinValue(val portId: Int, val value: Int) : FirmataMessage {

    override fun sendTo(transport: Transport) {
        transport.write(DIGITAL_MESSAGE or (portId and 0x0F))
        transport.write(value and 0x7F)
        transport.write(value ushr 7 and 0x7F)
    }
}
