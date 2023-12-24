package kt.firmata.hardware

interface DigitalOutput<T> : Hardware, DigitalInput<T> where T : DigitalInput<T>, T : DigitalOutput<T> {

    fun on()

    fun off()

    fun toggle()
}
