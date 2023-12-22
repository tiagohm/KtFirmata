package kt.firmata.core

import java.util.function.Consumer

fun interface OnStopListener : Consumer<IOEvent>, IODeviceEventListener {

    override fun onStop(event: IOEvent) {
        accept(event)
    }
}
