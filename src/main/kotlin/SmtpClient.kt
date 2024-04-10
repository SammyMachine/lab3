import java.io.Closeable
import java.net.Socket
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

open class SmtpClient : Closeable {
    protected lateinit var socket: Socket
    open val port: Int = 25
    var isConnected = false
        protected set

    open fun connect(smtpServer: String, timeout: Duration = 10.seconds) {
        isConnected = true
        socket = Socket(smtpServer, port)
        socket.soTimeout = timeout.inWholeMilliseconds.toInt()
        socket.expectedResponse(220)
    }

    open fun <T> send(buildMessage: Message.() -> T) {
        require(isConnected) { "Missed connection" }
        with(socket) {
            val message = Message().apply { buildMessage() }
            expectedResponse(250, "HELO Alice")
            expectedResponse(250, "MAIL FROM: <${message.addressFrom}>")
            expectedResponse(250, "RCPT TO: <${message.addressTo}>")
            expectedResponse(354, "DATA")
            expectedResponse(250, message.content)
            expectedResponse(221, "QUIT")
        }
    }

    override fun close() {
        if (isConnected) {
            println("Connection closed")
            socket.close()
        }
    }
}



