package kt.firmata.hardware

import java.time.Duration

interface Hardware {

    val name: String

    fun start(freq: Duration = Duration.ZERO)

    fun stop()
}
