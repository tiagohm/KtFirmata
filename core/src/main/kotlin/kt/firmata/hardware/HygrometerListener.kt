package kt.firmata.hardware

fun interface HygrometerListener<in T : Hygrometer<*>> {

    fun onHumidityChange(hygrometer: T)
}
