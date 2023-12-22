package kt.firmata.core

data class IOEvent(
    val device: IODevice,
    val pin: Pin? = null,
    val value: Int = pin?.value ?: 0,
)
