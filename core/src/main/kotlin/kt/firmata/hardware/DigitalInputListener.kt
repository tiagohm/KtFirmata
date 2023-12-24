package kt.firmata.hardware

fun interface DigitalInputListener<in T : DigitalInput<*>> {

    fun onDigitalInputChange(digitalInput: T)
}
