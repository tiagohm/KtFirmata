package kt.firmata.hardware

import kt.firmata.core.IODevice
import kt.firmata.core.Pin
import kt.firmata.core.PinMode
import java.time.Duration

data class Button(override val board: IODevice, override val pin: Pin, val isPullUp: Boolean = false) : AbstractDigitalInput<Button>() {

    init {
        require(board.isPinDigital(pin) && !board.isPinLED(pin)) { "invalid digital pin: ${pin.index}" }
    }

    override fun start(period: Duration) {
        pin.mode = if (isPullUp) PinMode.PULL_UP else PinMode.INPUT
        super.start(period)
    }
}
