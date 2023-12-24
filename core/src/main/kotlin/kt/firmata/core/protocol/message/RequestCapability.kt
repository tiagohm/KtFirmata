package kt.firmata.core.protocol.message

import kt.firmata.core.IODevice
import kt.firmata.core.protocol.parser.FirmataToken.CAPABILITY_QUERY
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

data object RequestCapability : FirmataMessage {

    override fun sendTo(board: IODevice, transport: Transport) {
        transport.write(START_SYSEX)
        transport.write(CAPABILITY_QUERY)
        transport.write(END_SYSEX)
    }
}
