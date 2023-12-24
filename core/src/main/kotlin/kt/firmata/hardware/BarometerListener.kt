package kt.firmata.hardware

fun interface BarometerListener<in T : Barometer<*>> {

    fun onPressureChange(barometer: T)
}
