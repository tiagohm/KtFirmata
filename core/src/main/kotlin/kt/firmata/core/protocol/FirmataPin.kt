package kt.firmata.core.protocol

import kt.firmata.core.IOEvent
import kt.firmata.core.Pin
import kt.firmata.core.PinEventListener
import kt.firmata.core.PinMode
import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.message.*
import java.util.*
import kotlin.concurrent.Volatile

data class FirmataPin(override val device: Board, override val index: Int) : Pin {

    private val listeners = Collections.synchronizedSet(HashSet<PinEventListener>())

    override val supportedModes: MutableSet<PinMode> = Collections.synchronizedSet(EnumSet.noneOf(PinMode::class.java))

    @Volatile private var currentMode = PinMode.UNSUPPORTED
    @Volatile private var currentValue = 0

    override var mode
        get() = currentMode
        set(mode) {
            // Arduino defaults (https://www.arduino.cc/en/Reference/ServoAttach)
            updateMode(mode, 544, 2400)
        }

    override fun servoMode(minPulse: Int, maxPulse: Int) {
        updateMode(PinMode.SERVO, minPulse, maxPulse)
    }

    @Synchronized
    private fun updateMode(mode: PinMode, minPulse: Int, maxPulse: Int) {
        if (supports(mode)) {
            if (currentMode != mode) {
                if (mode == PinMode.SERVO) {
                    device.sendMessage(ServoConfig(this, minPulse, maxPulse))
                    // The currentValue for a servo is unknown as the motor is
                    // send to the 1.5ms position when pinStateRequest is invoked
                    currentValue = -1
                }

                device.sendMessage(SetMode(index, mode))
                currentMode = mode

                val event = IOEvent(device, this)
                device.pinChanged(event)

                for (listener in listeners) {
                    listener.onModeChange(event)
                }

                device.sendMessage(PinStateRequest(index))
            }
        } else {
            throw IllegalArgumentException("Pin $index does not support mode $mode")
        }
    }

    override fun supports(mode: PinMode): Boolean {
        return supportedModes.contains(mode)
    }

    override var value
        get() = currentValue
        @Synchronized set(value) {
            val message: FirmataMessage
            val newValue: Int

            when (currentMode) {
                PinMode.OUTPUT -> {
                    val portId = index / 8
                    val pinInPort = index % 8
                    var portValue = 0

                    repeat(8) {
                        val p = device.pinAt(portId * 8 + it)

                        if (p.mode == PinMode.OUTPUT && p.value > 0) {
                            portValue = portValue or (1 shl it)
                        }
                    }

                    val bit = 1 shl pinInPort
                    val isOn = value > 0

                    portValue = if (isOn) {
                        portValue or bit
                    } else {
                        portValue and bit.inv()
                    }

                    message = DigitalWrite(portId, portValue)
                    newValue = if (isOn) 1 else 0
                }
                PinMode.ANALOG,
                PinMode.PWM,
                PinMode.SERVO -> {
                    message = AnalogWrite(this, value)
                    newValue = value
                }
                else -> {
                    throw IllegalStateException("Port $index is in $currentMode mode and its value cannot be set.")
                }
            }

            if (currentValue != newValue) {
                device.sendMessage(message)
                updateValue(value)
            }
        }

    override fun addEventListener(listener: PinEventListener) {
        listeners.add(listener)
    }

    override fun removeEventListener(listener: PinEventListener) {
        listeners.remove(listener)
    }


    override fun removeAllEventListeners() {
        listeners.clear()
    }

    fun addSupportedMode(mode: PinMode) {
        supportedModes.add(mode)
    }

    @Synchronized
    fun initMode(mode: PinMode) {
        currentMode = mode
    }

    @Synchronized
    fun initValue(value: Int) {
        currentValue = value
    }

    @Synchronized
    fun updateValue(value: Int) {
        if (value != currentValue) {
            currentValue = value

            val event = IOEvent(device, this)

            device.pinChanged(event)

            for (listener in listeners) {
                listener.onValueChange(event)
            }
        }
    }

    override fun toString(): kotlin.String {
        return "FirmataPin(id=$index, mode=$currentMode, value=$currentValue)"
    }
}
