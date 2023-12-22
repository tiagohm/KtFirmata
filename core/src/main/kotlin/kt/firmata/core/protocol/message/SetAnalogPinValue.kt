package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.parser.FirmataToken.ANALOG_MESSAGE
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.EXTENDED_ANALOG
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

data class SetAnalogPinValue(val pinId: Int, val value: Int) : FirmataMessage {

    override fun sendTo(transport: Transport) {
        if (pinId <= 15 && value <= 16383) {
            transport.write(ANALOG_MESSAGE or (pinId and 0x0F))
            transport.write(value and 0x7F)
            transport.write(value ushr 7 and 0x7F)
        } else {
            transport.write(START_SYSEX)
            transport.write(EXTENDED_ANALOG)
            transport.write(pinId)
            transport.write(value and 0x7F)
            transport.write(value ushr 7 and 0x7F)
            transport.write(value ushr 14 and 0x7F)
            transport.write(value ushr 21 and 0x7F)
            transport.write(END_SYSEX)
        }
    }
}
