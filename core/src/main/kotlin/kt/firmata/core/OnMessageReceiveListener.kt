package kt.firmata.core

fun interface OnMessageReceiveListener : IODeviceEventListener {

    override fun onMessageReceive(event: IOEvent, message: String)
}
