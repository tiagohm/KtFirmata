package kt.firmata.core.protocol.parser

import kt.firmata.core.protocol.fsm.AbstractState
import kt.firmata.core.protocol.fsm.FiniteStateMachine
import kt.firmata.core.protocol.parser.FirmataToken.ANALOG_MAPPING_RESPONSE
import kt.firmata.core.protocol.parser.FirmataToken.CAPABILITY_RESPONSE
import kt.firmata.core.protocol.parser.FirmataToken.EXTENDED_ANALOG
import kt.firmata.core.protocol.parser.FirmataToken.I2C_REPLY
import kt.firmata.core.protocol.parser.FirmataToken.PIN_STATE_RESPONSE
import kt.firmata.core.protocol.parser.FirmataToken.REPORT_FIRMWARE
import kt.firmata.core.protocol.parser.FirmataToken.STRING_DATA

data class ParsingSysexMessageState(override val finiteStateMashine: FiniteStateMachine) : AbstractState() {

    override fun process(b: Int) {
        val nextState = STATES[b]

        if (nextState == null) {
            val newState = ParsingCustomSysexMessageState(finiteStateMashine)
            newState.process(b)
            transitTo(newState)
        } else {
            transitTo(nextState)
        }
    }

    companion object {

        @JvmStatic private val STATES = mapOf(
            REPORT_FIRMWARE to ParsingFirmwareMessageState::class.java,
            EXTENDED_ANALOG to ParsingExtendedAnalogMessageState::class.java,
            CAPABILITY_RESPONSE to ParsingCapabilityResponseState::class.java,
            ANALOG_MAPPING_RESPONSE to ParsingAnalogMappingState::class.java,
            PIN_STATE_RESPONSE to PinStateParsingState::class.java,
            STRING_DATA to ParsingStringMessageState::class.java,
            I2C_REPLY to ParsingI2CMessageState::class.java,
        )
    }
}
