package kt.firmata.core.protocol.message

import kt.firmata.core.IODevice
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.REPORT_FIRMWARE
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

data object RequestFirmware : FirmataMessage {

    override fun sendTo(board: IODevice, transport: Transport) {
        transport.write(START_SYSEX)
        transport.write(REPORT_FIRMWARE)
        transport.write(END_SYSEX)
    }
}
