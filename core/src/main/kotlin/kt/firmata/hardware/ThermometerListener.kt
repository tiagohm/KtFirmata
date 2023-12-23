package kt.firmata.hardware

fun interface ThermometerListener<in T : Thermometer> {

    fun onTemperatureChange(thermometer: T)
}
