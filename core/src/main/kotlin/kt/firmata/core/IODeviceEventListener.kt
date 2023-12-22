package kt.firmata.core

interface IODeviceEventListener {

    fun onStart(event: IOEvent) = Unit

    fun onStop(event: IOEvent) = Unit

    fun onPinChange(event: IOEvent) = Unit

    fun onMessageReceive(event: IOEvent, message: String) = Unit
}
