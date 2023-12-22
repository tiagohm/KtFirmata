package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.I2C_CONFIG
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

data class I2CConfigRequest(val delayInMicroseconds: Int) : FirmataMessage {

    init {
        require(delayInMicroseconds >= 0) { "delay cannot be less than 0 microseconds." }
        require(delayInMicroseconds <= 255) { "delay cannot be greater than 255 microseconds." }
    }

    override fun sendTo(transport: Transport) {
        val delayLsb = delayInMicroseconds and 0x7F
        val delayMsb = if (delayInMicroseconds > 128) 1 else 0
        transport.write(START_SYSEX)
        transport.write(I2C_CONFIG)
        transport.write(delayLsb)
        transport.write(delayMsb)
        transport.write(END_SYSEX)
    }
}
