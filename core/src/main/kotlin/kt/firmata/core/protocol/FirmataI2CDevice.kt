package kt.firmata.core.protocol

import kt.firmata.core.I2CDevice
import kt.firmata.core.I2CEvent
import kt.firmata.core.I2CListener
import kt.firmata.core.protocol.board.Board
import kt.firmata.core.protocol.message.I2CReadRequest
import kt.firmata.core.protocol.message.I2CStopContinuousRequest
import kt.firmata.core.protocol.message.I2CWriteRequest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicBoolean

data class FirmataI2CDevice internal constructor(private val master: Board, override val address: Int) : I2CDevice {

    private val receivingUpdates = AtomicBoolean(false)
    private val callbacks = ConcurrentHashMap<Int, I2CListener>()
    private val subscribers = ConcurrentSkipListSet<I2CListener>()

    override fun delay(delay: Int) {
        master.i2cDelay(delay)
    }

    override fun tell(data: ByteArray) {
        master.sendMessage(I2CWriteRequest(address, data))
    }

    override fun ask(responseLength: Int, listener: I2CListener) {
        ask(REGISTER_NOT_SET, responseLength, listener)
    }

    @Synchronized
    override fun ask(register: Int, responseLength: Int, listener: I2CListener) {
        callbacks[register] = listener
        master.sendMessage(I2CReadRequest(address, register, responseLength, false))
    }

    override fun subscribe(listener: I2CListener) {
        subscribers.add(listener)
    }

    override fun unsubscribe(listener: I2CListener) {
        subscribers.remove(listener)
    }

    override fun startReceivingUpdates(register: Int, messageLength: Int): Boolean {
        val result = receivingUpdates.compareAndSet(false, true)

        if (result) {
            master.sendMessage(I2CReadRequest(address, register, messageLength, true))
        }

        return result
    }

    override fun startReceivingUpdates(messageLength: Int): Boolean {
        val result = receivingUpdates.compareAndSet(false, true)

        if (result) {
            master.sendMessage(I2CReadRequest(address, REGISTER_NOT_SET, messageLength, true))
        }

        return result
    }

    override fun stopReceivingUpdates() {
        if (receivingUpdates.compareAndSet(true, false)) {
            master.sendMessage(I2CStopContinuousRequest(address))
        }
    }

    internal fun onReceive(register: Int, message: ByteArray) {
        val event = I2CEvent(this, register, message)
        val listener = callbacks.remove(register)

        if (listener == null) {
            for (subscriber in subscribers) {
                subscriber.onReceive(event)
            }
        } else {
            listener.onReceive(event)
        }
    }

    override fun toString(): String {
        return "FirmataI2CDevice(address=$address)"
    }

    companion object {

        const val REGISTER_NOT_SET = -1
    }
}
