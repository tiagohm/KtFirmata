package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.fsm.I2CMessageEvent
import kt.firmata.core.protocol.parser.FirmataToken.END_SYSEX
import kotlin.math.floor

data class ParsingI2CMessageState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    override fun process(b: Int) {
        if (b == END_SYSEX) {
            val data = buf.parseI2CMessage()
            val address = data[0].toInt() and 0xFF
            val register = data[1].toInt() and 0xFF
            val message = data.sliceArray(2 until data.size)
            publish(I2CMessageEvent(address, register, message))
            transitTo<WaitingForMessageState>()
        } else {
            write(b)
        }
    }

    companion object {

        @JvmStatic
        private fun ByteArray.parseI2CMessage(): ByteArray {
            val outSize = floor((size / 2).toDouble()).toInt()
            val outBuffer = ByteArray(outSize)
            var outIndex = 0
            var index = 0

            while (index < size) {
                outBuffer[outIndex] = ((this[index + 1].toInt() and 0x01 shl 7) or (this[index].toInt() and 0x7F)).toByte()
                outIndex++
                index += 2
            }

            return outBuffer
        }
    }
}
