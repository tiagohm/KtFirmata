package kt.firmata.hardware

import java.io.Closeable
import java.time.Duration

interface Hardware : Closeable {

    val name: String

    fun start(period: Duration = Duration.ZERO)
}
