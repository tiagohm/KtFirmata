package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.FirmataI2CDevice
import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.I2C_READ
import kt.firmata.core.protocol.parser.FirmataToken.I2C_READ_CONTINUOUS
import kt.firmata.core.protocol.parser.FirmataToken.I2C_REQUEST
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

// https://github.com/firmata/protocol/blob/master/i2c.md

data class I2CReadRequest(val slaveAddress: Int, val register: Int, val bytesToRead: Int, val continuous: Boolean = false) : FirmataMessage {

    override fun sendTo(board: Board, transport: Transport) {
        transport.write(START_SYSEX)
        transport.write(I2C_REQUEST)
        transport.write(slaveAddress)
        transport.write(if (continuous) I2C_READ_CONTINUOUS else I2C_READ)

        // TODO: replace hardcoded slave address (MSB) with generated one to support 10-bit mode

        if (register != FirmataI2CDevice.REGISTER_NOT_SET) {
            transport.write(register and 0x7F)
            transport.write(register ushr 7 and 0x7F)
        }

        transport.write(bytesToRead and 0x7F)
        transport.write(bytesToRead ushr 7 and 0x7F)

        transport.write(END_SYSEX)
    }
}
