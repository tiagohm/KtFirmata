package kt.firmata.hardware

interface DigitalInput<T : DigitalInput<T>> : Hardware {

    val value: Boolean

    fun registerDigitalInputListener(listener: DigitalInputListener<T>)

    fun unregisterDigitalInputListener(listener: DigitalInputListener<T>)
}
