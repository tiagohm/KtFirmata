package kt.firmata.core.protocol.message

import kt.firmata.core.IODevice
import kt.firmata.core.protocol.parser.FirmataToken.REPORT_ANALOG
import kt.firmata.core.protocol.transport.Transport

data class ReportAnalog(val enabled: Boolean) : FirmataMessage {

    override fun sendTo(board: IODevice, transport: Transport) {
        repeat(16) {
            transport.write(REPORT_ANALOG or it)
            transport.write(if (enabled) 1 else 0)
        }
    }
}
