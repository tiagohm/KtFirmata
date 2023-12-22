package kt.firmata.core.protocol.message

import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kt.firmata.core.protocol.parser.FirmataToken.SERVO_CONFIG
import kt.firmata.core.protocol.parser.FirmataToken.START_SYSEX
import kt.firmata.core.protocol.transport.Transport

//https://github.com/firmata/protocol/blob/master/servos.md

data class ServoConfig(val pinId: Int, val minPulse: Int, val maxPulse: Int) : FirmataMessage {

    override fun sendTo(transport: Transport) {
        transport.write(START_SYSEX)
        transport.write(SERVO_CONFIG)
        transport.write(pinId)
        transport.write(minPulse and 0x7F)
        transport.write(minPulse ushr 7 and 0x7F)
        transport.write(maxPulse and 0x7F)
        transport.write(maxPulse ushr 7 and 0x7F)
        transport.write(END_SYSEX)
    }
}
