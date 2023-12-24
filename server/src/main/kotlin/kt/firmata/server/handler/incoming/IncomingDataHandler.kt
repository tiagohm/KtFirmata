package kt.firmata.server.handler.incoming

interface IncomingDataHandler {

    val topic: String

    fun handle(topic: String, data: String): Boolean
}
