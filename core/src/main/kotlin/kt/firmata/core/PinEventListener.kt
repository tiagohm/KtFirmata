package kt.firmata.core

interface PinEventListener {

    fun onModeChange(event: IOEvent) = Unit

    fun onValueChange(event: IOEvent) = Unit
}
