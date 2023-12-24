package kt.firmata.core

import kt.firmata.core.protocol.fsm.Event
import kt.firmata.core.protocol.message.FirmataMessage
import java.io.Closeable
import java.util.function.Consumer

interface IODevice : Closeable {

    fun start()

    fun ensureInitializationIsDone()

    val isReady: Boolean

    val pins: Collection<Pin>

    val pinsCount: Int

    fun pinAt(index: Int): Pin

    fun i2CDevice(address: Int): I2CDevice

    fun addEventListener(listener: IODeviceEventListener)

    fun removeEventListener(listener: IODeviceEventListener)

    fun <T : Event> addProtocolMessageHandler(type: Class<out T>, handler: Consumer<in T>)

    fun sendMessage(message: String)

    fun sendMessage(message: FirmataMessage)
}
