package kt.firmata.core

@Suppress("ArrayInDataClass")
data class I2CEvent(
    val device: I2CDevice? = null,
    val register: Int = 0,
    val data: ByteArray = ByteArray(0),
)
