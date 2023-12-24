package kt.firmata.hardware

interface Thermometer<T : Thermometer<T>> : Hardware {

    val temperature: Double

    fun registerThermometerListener(listener: ThermometerListener<T>)

    fun unregisterThermometerListener(listener: ThermometerListener<T>)
}
