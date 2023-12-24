package kt.firmata.core.protocol.message

import kt.firmata.core.IODevice
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.I2C_REQUEST
import kt.firmata.core.protocol.parser.FirmataToken.I2C_WRITE
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

// https://github.com/firmata/protocol/blob/master/i2c.md

@Suppress("ArrayInDataClass")
data class I2CWriteRequest(val slaveAddress: Int, val bytesToWrite: ByteArray) : FirmataMessage {

    override fun sendTo(board: IODevice, transport: Transport) {
        transport.write(START_SYSEX)
        transport.write(I2C_REQUEST)
        transport.write(slaveAddress)
        transport.write(I2C_WRITE)

        // TODO: replace I2C_WRITE with generated slave address (MSB) to support 10-bit mode.

        for (x in bytesToWrite.indices) {
            transport.write(bytesToWrite[x].toInt() and 0x7F)
            transport.write(bytesToWrite[x].toInt() and 0xFF ushr 7 and 0x7F)
        }

        transport.write(END_SYSEX)
    }
}
