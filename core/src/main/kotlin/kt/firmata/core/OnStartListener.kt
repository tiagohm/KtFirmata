package kt.firmata.core

import java.util.function.Consumer

fun interface OnStartListener : Consumer<IOEvent>, IODeviceEventListener {

    override fun onStart(event: IOEvent) {
        accept(event)
    }
}
