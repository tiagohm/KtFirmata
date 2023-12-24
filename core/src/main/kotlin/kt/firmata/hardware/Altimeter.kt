package kt.firmata.hardware

interface Altimeter<T : Altimeter<T>> : Hardware {

    val altitude: Double

    fun registerAltimeterListener(listener: AltimeterListener<T>)

    fun unregisterAltimeterListener(listener: AltimeterListener<T>)
}
