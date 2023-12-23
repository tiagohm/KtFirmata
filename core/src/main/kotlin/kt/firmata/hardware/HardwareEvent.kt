package kt.firmata.hardware

interface HardwareEvent<out T : Hardware> {

    val hardware: T
}
