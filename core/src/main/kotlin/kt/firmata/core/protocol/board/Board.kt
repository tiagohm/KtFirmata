package kt.firmata.core.protocol.board

import kt.firmata.core.*
import kt.firmata.core.protocol.DaemonThreadFactory
import kt.firmata.core.protocol.FirmataI2CDevice
import kt.firmata.core.protocol.FirmataPin
import kt.firmata.core.protocol.fsm.*
import kt.firmata.core.protocol.message.*
import kt.firmata.core.protocol.parser.FirmataParser
import kt.firmata.core.protocol.parser.WaitingForMessageState
import kt.firmata.core.protocol.transport.Transport
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

abstract class Board(
    private val transport: Transport,
) : IODevice {

    private val protocol: FiniteStateMachine = FiniteStateMachine(WaitingForMessageState::class.java)
    private val parser = FirmataParser(protocol)

    private val listeners = Collections.synchronizedSet(LinkedHashSet<IODeviceEventListener>())
    private val pinStateRequestQueue = ArrayDeque<Int>()
    private val started = AtomicBoolean(false)
    private val ready = AtomicBoolean(false)
    private val initializedPins = AtomicInteger(0)
    private val longestI2CDelay = AtomicInteger(0)
    private val i2cDevices = HashMap<Int, FirmataI2CDevice>()
    private val analogMapping = HashMap<Int, Int>()
    private val foundPins = Collections.synchronizedMap(LinkedHashMap<Int, FirmataPin>())

    init {
        transport.parser = parser
    }

    abstract val numberOfDigitalPins: Int
    abstract val numberOfAnalogPins: Int

    open fun isPinBlink(pin: Pin) = false
    abstract fun isPinDigital(pin: Pin): Boolean
    abstract fun isPinAnalog(pin: Pin): Boolean
    abstract fun isPinPWM(pin: Pin): Boolean
    abstract fun isPinServo(pin: Pin): Boolean
    abstract fun isPinI2C(pin: Pin): Boolean
    abstract fun isPinSPI(pin: Pin): Boolean

    abstract fun pinToDigitalIndex(pin: Pin): Int
    abstract fun pinToAnalogIndex(pin: Pin): Int
    abstract fun pinToPWMIndex(pin: Pin): Int
    abstract fun pinToServoIndex(pin: Pin): Int

    final override fun start() {
        if (!started.getAndSet(true)) {
            try {
                parser.start()
                transport.start()
                sendMessage(RequestFirmware)
            } catch (e: IOException) {
                transport.close()
                parser.stop()
                throw e
            }
        }
    }

    final override fun stop() {
        shutdown()

        val event = IOEvent(this)

        for (listener in listeners) {
            listener.onStop(event)
        }
    }

    final override fun ensureInitializationIsDone() {
        if (!started.get()) {
            try {
                start()
            } catch (e: IOException) {
                throw InterruptedException(e.message)
            }
        }

        var timePassed = 0L
        val timeout: Long = 100

        while (!isReady) {
            if (timePassed >= TIMEOUT) {
                throw InterruptedException(
                    """
    Connection timeout.
    Please, make sure the board runs a firmware that supports Firmata protocol.
    The firmware has to implement callbacks for CAPABILITY_QUERY, PIN_STATE_QUERY and ANALOG_MAPPING_QUERY in order for the initialization to work.
    """.trimIndent()
                )
            }

            timePassed += timeout
            Thread.sleep(timeout)
        }
    }

    final override val isReady
        get() = ready.get()

    final override fun addEventListener(listener: IODeviceEventListener) {
        listeners.add(listener)
    }

    final override fun removeEventListener(listener: IODeviceEventListener) {
        listeners.remove(listener)
    }

    final override val pins
        get() = foundPins.values

    final override val pinsCount
        get() = foundPins.size

    final override fun pinAt(index: Int): Pin {
        return foundPins[index]!!
    }

    @Synchronized
    final override fun i2CDevice(address: Int): I2CDevice? {
        if (address !in i2cDevices) {
            i2cDevices[address] = FirmataI2CDevice(this, address)
        }

        sendMessage(I2CConfigRequest(longestI2CDelay.get()))

        return i2cDevices[address]
    }

    final override fun <T : Event> addProtocolMessageHandler(type: Class<out T>, handler: Consumer<in T>) {
        protocol.addHandler(type, handler)
    }

    final override fun sendMessage(message: FirmataMessage) {
        message.sendTo(this, transport)
        transport.flush()
    }

    final override fun sendMessage(message: String) {
        if (message.length > 15) {
            LOG.warn("Firmata 2.3.6 implementation has input buffer only 32 bytes so you can safely send only 15 characters log messages")
        }

        sendMessage(Text(message))
    }

    internal fun pinChanged(event: IOEvent) {
        for (listener in listeners) {
            listener.onPinChange(event)
        }
    }

    fun i2cDelay(delay: Int) {
        val message = I2CConfigRequest(delay)
        var longestDelaySoFar = longestI2CDelay.get()

        while (longestDelaySoFar < delay) {
            if (longestI2CDelay.compareAndSet(longestDelaySoFar, delay)) {
                sendMessage(message)
            }

            longestDelaySoFar = longestI2CDelay.get()
        }
    }

    private fun shutdown() {
        ready.set(false)
        sendMessage(ReportAnalog(false))
        sendMessage(ReportDigital(false))
        parser.stop()
        transport.close()
    }

    override fun toString(): String {
        return "Board(transport=$transport, pins=$foundPins, i2cDevices=$i2cDevices)"
    }

    private val onProtocolReceive = Consumer<VersionMessageEvent> {
        LOG.info("Firmware version. major={}, minor={}", it.major, it.minor)
    }

    private val onFirmwareReceive = Consumer<FirmwareMessageEvent> {
        LOG.info("Firmware message. major={}, minor={}, message={}", it.major, it.minor, it.message)
        sendMessage(RequestCapability)
    }

    private val onCapabilitiesReceive = Consumer<PinCapabilityResponseEvent> {
        val pin = FirmataPin(this@Board, it.pinId)

        it.supportedModes.forEach(pin::addSupportedMode)

        foundPins[pin.index] = pin

        if (pin.supportedModes.isEmpty()) {
            // if the pin has no supported modes, its initialization is already done.
            initializedPins.incrementAndGet()
        } else {
            // if the pin supports some modes, we ask for its current mode and value.
            pinStateRequestQueue.add(it.pinId)
        }
    }

    private val onCapabilitiesFinished = Consumer<PinCapabilitiesFinishedEvent> {
        if (initializedPins.get() == foundPins.size) {
            sendMessage(RequestAnalogMapping)
        } else {
            val pinId = pinStateRequestQueue.poll()
            sendMessage(PinStateRequest(pinId))
        }
    }

    private val onPinStateReceive = Consumer<PinStateEvent> {
        val pin = foundPins[it.pinId] ?: return@Consumer

        if (pin.mode == PinMode.UNSUPPORTED) {
            pin.initMode(PinMode.resolve(it.mode))
        }

        if (!pinStateRequestQueue.isEmpty()) {
            val pid = pinStateRequestQueue.poll()
            sendMessage(PinStateRequest(pid))
        }

        if (initializedPins.incrementAndGet() == foundPins.size) {
            sendMessage(RequestAnalogMapping)
        }
    }

    private val onAnalogMappingReceive = Consumer<AnalogMappingEvent> {
        synchronized(analogMapping) {
            analogMapping.putAll(it.mapping)

            ready.set(true)

            // All the pins are initialized so notification is sent to listeners
            val event = IOEvent(this@Board)

            for (listener in listeners) {
                listener.onStart(event)
            }
        }
    }

    private val onAnalogMessageReceive = Consumer<AnalogMessageEvent> {
        synchronized(analogMapping) {
            if (it.pinId in analogMapping) {
                val pinId = analogMapping[it.pinId]!!

                val pin = foundPins[pinId] ?: return@Consumer

                if (pin.mode == PinMode.ANALOG) {
                    pin.updateValue(it.value)
                }
            }
        }
    }

    private val onDigitalMessageReceive = Consumer<DigitalMessageEvent> {
        val pin = foundPins[it.pinId] ?: return@Consumer

        if (pin.mode == PinMode.INPUT || pin.mode == PinMode.PULL_UP) {
            pin.updateValue(it.value)
        }
    }

    private val onI2cMessageReceive = Consumer<I2CMessageEvent> {
        val device = i2cDevices[it.address]
        device?.onReceive(it.register, it.message)
    }

    private val onStringMessageReceive = Consumer<StringMessageEvent> {
        val evt = IOEvent(this@Board)

        for (listener in listeners) {
            listener.onMessageReceive(evt, it.message)
        }
    }

    init {
        val executor = Executors.newSingleThreadExecutor(THREAD_FACTORY)

        addEventListener(OnStopListener {
            executor.shutdown()

            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow()

                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        LOG.error("Cannot stop an event handling executor. It may result in a thread leak.")
                    }
                }
            } catch (e: InterruptedException) {
                executor.shutdownNow()
                Thread.currentThread().interrupt()
            }
        })

        protocol.eventHandlingExecutor = executor

        protocol.addHandler(onProtocolReceive)
        protocol.addHandler(onFirmwareReceive)
        protocol.addHandler(onCapabilitiesReceive)
        protocol.addHandler(onCapabilitiesFinished)
        protocol.addHandler(onPinStateReceive)
        protocol.addHandler(onAnalogMappingReceive)
        protocol.addHandler(onAnalogMessageReceive)
        protocol.addHandler(onDigitalMessageReceive)
        protocol.addHandler(onI2cMessageReceive)
        protocol.addHandler(onStringMessageReceive)

        protocol.addHandler<FiniteStateMachineInTerminalStateEvent> {
            LOG.error("Parser has reached the terminal state. It may be due receiving of unsupported command.")
        }
    }

    companion object {

        private const val TIMEOUT = 15000L

        @JvmStatic private val THREAD_FACTORY = DaemonThreadFactory("Firmata-Event-Handler")
        @JvmStatic private val LOG = LoggerFactory.getLogger(Board::class.java)
    }
}
