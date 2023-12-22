package kt.firmata.core

import kt.firmata.core.protocol.parser.FirmataToken.PIN_MODE_IGNORE
import kt.firmata.core.protocol.parser.FirmataToken.TOTAL_PIN_MODES

enum class PinMode {
    INPUT,
    OUTPUT,
    ANALOG,
    PWM,
    SERVO,
    SHIFT,
    I2C,
    ONE_WIRE,
    STEPPER,
    ENCODER,
    SERIAL,
    PULL_UP,
    UNSUPPORTED,
    IGNORED;

    companion object {

        @JvmStatic
        fun resolve(modeToken: Int) = if (modeToken == PIN_MODE_IGNORE) IGNORED
        else if (modeToken > TOTAL_PIN_MODES) UNSUPPORTED
        else entries[modeToken]
    }
}
