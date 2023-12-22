package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.parser.FirmataToken.REPORT_ANALOG
import kt.firmata.core.protocol.transport.Transport

data class AnalogReport(val enabled: Boolean) : FirmataMessage {

    override fun sendTo(transport: Transport) {
        repeat(16) {
            transport.write(REPORT_ANALOG or it)
            transport.write(if (enabled) 1 else 0)
        }
    }
}
