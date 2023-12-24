package kt.firmata.core.protocol.message

import kt.firmata.core.IODevice
import kt.firmata.core.Pin
import kt.firmata.core.protocol.parser.FirmataToken.ANALOG_MESSAGE
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.EXTENDED_ANALOG
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

data class AnalogWrite(val pin: Pin, val value: Int) : FirmataMessage {

    override fun sendTo(board: IODevice, transport: Transport) {
        if (pin.index <= 15) {
            transport.write(ANALOG_MESSAGE or (pin.index and 0x0F))
            transport.write(value and 0x7F)
            transport.write(value ushr 7 and 0x7F)
        } else {
            transport.write(START_SYSEX)
            transport.write(EXTENDED_ANALOG)
            transport.write(pin.index)
            transport.write(value and 0x7F)
            transport.write(value ushr 7 and 0x7F)

            if (value > 0x00004000) {
                transport.write(value ushr 14 and 0x7F)
            }

            if (value > 0x00200000) {
                transport.write(value ushr 21 and 0x7F)
            }

            if (value > 0x10000000) {
                transport.write(value ushr 28 and 0x7F)
            }

            transport.write(END_SYSEX)
        }
    }
}
