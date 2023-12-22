package kt.firmata.core

interface Pin {

    val device: IODevice

    val index: Int

    var mode: PinMode

    fun servoMode(minPulse: Int, maxPulse: Int)

    fun supports(mode: PinMode): Boolean

    val supportedModes: Set<PinMode>

    var value: Int

    fun addEventListener(listener: PinEventListener)

    fun removeEventListener(listener: PinEventListener)

    fun removeAllEventListeners()
}
