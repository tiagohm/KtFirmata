package kt.firmata.hardware

import kt.firmata.core.IODevice
import kt.firmata.core.Pin
import kt.firmata.core.PinMode
import java.time.Duration

data class Led(override val board: IODevice, override val pin: Pin) : AbstractDigitalInput<Led>(), DigitalOutput<Led> {

    init {
        require(board.isPinDigital(pin)) { "invalid digital pin: ${pin.index}" }
    }

    override fun on() {
        pin.value = 1
    }

    override fun off() {
        pin.value = 0
    }

    override fun toggle() {
        pin.value = (pin.value xor 1) and 0x01
    }

    override fun start(period: Duration) {
        pin.mode = PinMode.OUTPUT
        super.start(period)
    }
}
