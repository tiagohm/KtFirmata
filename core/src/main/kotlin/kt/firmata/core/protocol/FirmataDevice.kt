package kt.firmata.core.protocol

import kt.firmata.core.*
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

data class FirmataDevice(
    private val transport: Transport,
    private val protocol: FiniteStateMachine = FiniteStateMachine(WaitingForMessageState::class.java),
) : IODevice {

    private val parser = FirmataParser(protocol)

    private val listeners = Collections.synchronizedSet(LinkedHashSet<IODeviceEventListener>())
    private val pinStateRequestQueue = ArrayDeque<Int>()
    private val started = AtomicBoolean(false)
    private val ready = AtomicBoolean(false)
    private val initializedPins = AtomicInteger(0)
    private val longestI2CDelay = AtomicInteger(0)
    private val i2cDevices = HashMap<Int, FirmataI2CDevice>()
    private val analogMapping = HashMap<Int, Int>()

    override val pins: MutableList<FirmataPin> = Collections.synchronizedList(ArrayList())

    override fun start() {
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

    override fun stop() {
        shutdown()

        val event = IOEvent(this)

        for (listener in listeners) {
            listener.onStop(event)
        }
    }

    override fun ensureInitializationIsDone() {
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

    override val isReady
        get() = ready.get()

    override fun addEventListener(listener: IODeviceEventListener) {
        listeners.add(listener)
    }

    override fun removeEventListener(listener: IODeviceEventListener) {
        listeners.remove(listener)
    }

    override val pinsCount
        get() = pins.size

    override fun pinAt(index: Int): Pin {
        return pins[index]
    }

    @Synchronized
    override fun i2CDevice(address: Int): I2CDevice? {
        if (address !in i2cDevices) {
            i2cDevices[address] = FirmataI2CDevice(this, address)
        }

        sendMessage(I2CConfigRequest(longestI2CDelay.get()))

        return i2cDevices[address]
    }

    override fun <T : Event> addProtocolMessageHandler(type: Class<out T>, handler: Consumer<in T>) {
        protocol.addHandler(type, handler)
    }

    override fun sendMessage(message: FirmataMessage) {
        message.sendTo(transport)
        transport.flush()
    }

    override fun sendMessage(message: String) {
        if (message.length > 15) {
            LOG.warn("Firmata 2.3.6 implementation has input buffer only 32 bytes so you can safely send only 15 characters log messages")
        }

        sendMessage(Text(message))
    }

    fun pinChanged(event: IOEvent) {
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
        sendMessage(AnalogReport(false))
        sendMessage(DigitalReport(false))
        parser.stop()
        transport.close()
    }

    private val onProtocolReceive = Consumer<VersionMessageEvent> {
        LOG.info("Firmware version. major={}, minor={}", it.major, it.minor)
    }

    private val onFirmwareReceive = Consumer<FirmwareMessageEvent> {
        LOG.info("Firmware message. major={}, minor={}, message={}", it.major, it.minor, it.message)
        sendMessage(RequestCapability)
    }

    private val onCapabilitiesReceive = Consumer<PinCapabilityResponseEvent> {
        val pin = FirmataPin(this@FirmataDevice, it.pinId)

        it.supportedModes.forEach(pin::addSupportedMode)

        pins.add(pin.index, pin)

        if (pin.supportedModes.isEmpty()) {
            // if the pin has no supported modes, its initialization is already done.
            initializedPins.incrementAndGet()
        } else {
            // if the pin supports some modes, we ask for its current mode and value.
            pinStateRequestQueue.add(it.pinId)
        }
    }

    private val onCapabilitiesFinished = Consumer<PinCapabilitiesFinishedEvent> {
        if (initializedPins.get() == pins.size) {
            sendMessage(RequestAnalogMapping)
        } else {
            val pinId = pinStateRequestQueue.poll()
            sendMessage(PinStateRequest(pinId))
        }
    }

    private val onPinStateReceive = Consumer<PinStateEvent> {
        val pin = pins[it.pinId]

        if (pin.mode == PinMode.NOT_INITIALIZED) {
            pin.initMode(PinMode.resolve(it.mode))
        }

        if (!pinStateRequestQueue.isEmpty()) {
            val pid = pinStateRequestQueue.poll()
            sendMessage(PinStateRequest(pid))
        }

        if (initializedPins.incrementAndGet() == pins.size) {
            sendMessage(RequestAnalogMapping)
        }
    }

    private val onAnalogMappingReceive = Consumer<AnalogMappingEvent> {
        synchronized(analogMapping) {
            analogMapping.putAll(it.mapping)

            sendMessage(AnalogReport(true))
            sendMessage(DigitalReport(true))

            ready.set(true)

            // all the pins are initialized so notification is sent to listeners
            val initIsDone = IOEvent(this@FirmataDevice)

            for (l in listeners) {
                l.onStart(initIsDone)
            }
        }
    }

    private val onAnalogMessageReceive = Consumer<AnalogMessageEvent> {
        synchronized(analogMapping) {
            if (it.pinId in analogMapping) {
                val pinId = analogMapping[it.pinId]!!

                if (pinId < pins.size) {
                    val pin = pins[pinId]

                    if (pin.mode == PinMode.ANALOG) {
                        pin.updateValue(it.value)
                    }
                }
            }
        }
    }

    private val onDigitalMessageReceive = Consumer<DigitalMessageEvent> {
        if (it.pinId < pins.size) {
            val pin = pins[it.pinId]

            if (pin.mode == PinMode.INPUT || pin.mode == PinMode.PULL_UP) {
                pin.updateValue(it.value)
            }
        }
    }

    private val onI2cMessageReceive = Consumer<I2CMessageEvent> {
        val device = i2cDevices[it.address]
        device?.onReceive(it.register, it.message)
    }

    private val onStringMessageReceive = Consumer<StringMessageEvent> {
        val evt = IOEvent(this@FirmataDevice)

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
        @JvmStatic private val LOG = LoggerFactory.getLogger(FirmataDevice::class.java)
    }
}
