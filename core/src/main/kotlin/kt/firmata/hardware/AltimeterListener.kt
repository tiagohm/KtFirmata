package kt.firmata.hardware

fun interface AltimeterListener<in T : Altimeter<*>> {

    fun onAltitudeChange(altimeter: T)
}
