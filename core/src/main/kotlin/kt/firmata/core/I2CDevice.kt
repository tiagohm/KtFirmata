package kt.firmata.core

interface I2CDevice {

    val address: Int

    fun delay(delay: Int)

    fun tell(data: ByteArray)

    fun ask(responseLength: Int, listener: I2CListener)

    fun ask(register: Int, responseLength: Int, listener: I2CListener)

    fun subscribe(listener: I2CListener)

    fun unsubscribe(listener: I2CListener)

    fun startReceivingUpdates(register: Int, messageLength: Int): Boolean

    fun startReceivingUpdates(messageLength: Int): Boolean

    fun stopReceivingUpdates()
}
