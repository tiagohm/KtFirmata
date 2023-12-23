package kt.firmata.hardware

import java.time.Duration

interface Hardware {

    fun start(freq: Duration = Duration.ZERO)

    fun stop()
}
