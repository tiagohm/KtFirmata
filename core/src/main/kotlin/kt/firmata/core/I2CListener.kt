package kt.firmata.core

fun interface I2CListener {

    fun onReceive(event: I2CEvent)
}
