package kt.firmata.server.handler.incoming

fun interface IncomingDataHandler {

    fun handle(topic: String, data: String): Boolean
}
