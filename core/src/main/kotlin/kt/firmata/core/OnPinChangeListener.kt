package kt.firmata.core

import java.util.function.Consumer

fun interface OnPinChangeListener : Consumer<IOEvent>, IODeviceEventListener {

    override fun onPinChange(event: IOEvent) {
        accept(event)
    }
}
