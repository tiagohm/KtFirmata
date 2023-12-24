package kt.firmata.core

import kt.firmata.core.protocol.fsm.Event
import kt.firmata.core.protocol.message.FirmataMessage
import java.io.Closeable
import java.util.function.Consumer

interface IODevice : Closeable {

    val numberOfDigitalPins: Int
    val numberOfAnalogPins: Int

    fun isPinLED(pin: Pin) = false
    fun isPinDigital(pin: Pin): Boolean
    fun isPinAnalog(pin: Pin): Boolean
    fun isPinPWM(pin: Pin): Boolean
    fun isPinServo(pin: Pin): Boolean
    fun isPinI2C(pin: Pin): Boolean
    fun isPinSPI(pin: Pin): Boolean

    fun pinToDigitalIndex(pin: Pin): Int
    fun pinToAnalogIndex(pin: Pin): Int
    fun pinToPWMIndex(pin: Pin): Int
    fun pinToServoIndex(pin: Pin): Int

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
