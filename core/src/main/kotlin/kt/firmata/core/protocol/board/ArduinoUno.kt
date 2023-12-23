package kt.firmata.core.protocol.board

import kt.firmata.core.Pin
import kt.firmata.core.protocol.transport.Transport

class ArduinoUno(transport: Transport) : Board(transport) {

    override val numberOfDigitalPins = 20

    override val numberOfAnalogPins = 8

    override fun isPinBlink(pin: Pin) = pin.index == 13

    override fun isPinDigital(pin: Pin) = pin.index in 2..19

    override fun isPinAnalog(pin: Pin) = pin.index in 14..21

    override fun isPinPWM(pin: Pin) = pin.index == 3 || pin.index == 5 || pin.index == 6 || pin.index == 9 || pin.index == 10 || pin.index == 11

    override fun isPinServo(pin: Pin) = isPinDigital(pin) && pin.index - 2 < 0

    override fun isPinI2C(pin: Pin) = pin.index == 18 || pin.index == 19

    override fun isPinSPI(pin: Pin) = pin.index in 10..13

    override fun pinToDigitalIndex(pin: Pin) = pin.index

    override fun pinToAnalogIndex(pin: Pin) = pin.index - 14

    override fun pinToPWMIndex(pin: Pin) = pin.index

    override fun pinToServoIndex(pin: Pin) = pin.index - 2

    companion object {

        const val SS = 10
        const val MOSI = 11
        const val MISO = 12
        const val SCK = 13

        const val SDA = 18
        const val SCL = 19

        const val A0 = 14
        const val A1 = 15
        const val A2 = 16
        const val A3 = 17
        const val A4 = 18
        const val A5 = 19
        const val A6 = 20
        const val A7 = 21

        const val LED_BUILTIN = 13
    }
}
