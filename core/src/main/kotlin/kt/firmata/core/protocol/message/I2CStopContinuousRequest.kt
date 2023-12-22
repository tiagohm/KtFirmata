package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.I2C_REQUEST
import kt.firmata.core.protocol.parser.FirmataToken.I2C_STOP_READ_CONTINUOUS
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

data class I2CStopContinuousRequest(val slaveAddress: Int) : FirmataMessage {

    override fun sendTo(transport: Transport) {
        transport.write(START_SYSEX)
        transport.write(I2C_REQUEST)
        transport.write(slaveAddress)
        transport.write(I2C_STOP_READ_CONTINUOUS)
        transport.write(END_SYSEX)
    }
}
