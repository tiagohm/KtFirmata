package kt.firmata.hardware

interface Barometer<T : Barometer<T>> : Hardware, Altimeter<T> {

    val pressure: Double

    fun registerBarometerListener(listener: BarometerListener<T>)

    fun unregisterBarometerListener(listener: BarometerListener<T>)
}
