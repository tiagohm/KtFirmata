package kt.firmata.core.protocol.board

import kt.firmata.core.Pin
import kt.firmata.core.protocol.transport.Transport

class ArduinoUno(transport: Transport) : Board(transport) {

    override val numberOfDigitalPins = 20

    override val numberOfAnalogPins = 8

    override fun isPinLED(pin: Pin) = pin.index == 13

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

        const val D0 = 0
        const val D1 = 1
        const val D2 = 2
        const val D3 = 3
        const val D4 = 4
        const val D5 = 5
        const val D6 = 6
        const val D7 = 7
        const val D8 = 8
        const val D9 = 9
        const val D10 = 10
        const val D11 = 11
        const val D12 = 12
        const val D13 = 13
        const val D14 = 14
        const val D15 = 15
        const val D16 = 16
        const val D17 = 17
        const val D18 = 18
        const val D19 = 19
        const val D20 = 20
        const val D21 = 21

        const val RX = D0
        const val TX = D1

        const val SS = D10
        const val MOSI = D11
        const val MISO = D12
        const val SCK = D13

        const val A0 = D14
        const val A1 = D15
        const val A2 = D16
        const val A3 = D17
        const val A4 = D18
        const val A5 = D19
        const val A6 = D20
        const val A7 = D21

        const val SDA = A4
        const val SCL = A5

        const val LED_BUILTIN = D13
    }
}
