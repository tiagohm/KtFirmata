package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.parser.FirmataToken.REPORT_DIGITAL
import kt.firmata.core.protocol.transport.Transport

data class DigitalReport(val enabled: Boolean) : FirmataMessage {

    override fun sendTo(transport: Transport) {
        repeat(16) {
            transport.write(REPORT_DIGITAL or it)
            transport.write(if (enabled) 1 else 0)
        }
    }
}
