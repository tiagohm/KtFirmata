package kt.firmata.core.protocol.transport

import kt.firmata.core.protocol.parser.Parser
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.SocketTimeoutException

data class NetworkTransport(private val ip: InetAddress, private val port: Int) : ByteArrayOutputStreamTransport(), Runnable {

    @Volatile private var socket: Socket? = null
    @Volatile private var input: InputStream? = null
    @Volatile private var output: OutputStream? = null
    @Volatile private var readerThread: Thread? = null

    override lateinit var parser: Parser

    override fun start() {
        socket = Socket(ip, port)
        socket!!.setReuseAddress(true)
        socket!!.setSoTimeout(1500)
        socket!!.setSoLinger(true, 1500)
        socket!!.setSoTimeout(1500)

        input = socket!!.getInputStream()
        output = socket!!.getOutputStream()

        readerThread = Thread(this, "Firmata-Network-Transport")
        readerThread!!.isDaemon = true
        readerThread!!.start()
    }

    override fun run() {
        val data = ByteArray(100)

        while (!Thread.currentThread().isInterrupted) {
            val byteCount = try {
                input!!.read(data)
            } catch (e: SocketTimeoutException) {
                break // We try to reconnect, hearthbeats (1*second) missing
            } catch (e: IOException) {
                break
            }

            if (byteCount == -1) {
                break
            }

            parser.parse(data.copyOfRange(0, byteCount))
        }
    }

    override fun close() {
        try {
            readerThread?.interrupt()
            readerThread?.join()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } finally {
            readerThread = null
        }

        runCatching { output?.close() }
        runCatching { input?.close() }
        runCatching { socket?.close() }

        output = null
        input = null
        socket = null
    }

    override fun flushBytes(bytes: ByteArray, size: Int) {
        output?.write(bytes, 0, size)
        output?.flush()
    }
}
