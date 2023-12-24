package kt.firmata.hardware

interface Hygrometer<T : Hygrometer<T>> : Hardware {

    val humidity: Double

    fun registerHygrometerListener(listener: HygrometerListener<T>)

    fun unregisterHygrometerListener(listener: HygrometerListener<T>)
}
