import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.time.Duration

class SmtpClientSSL : SmtpClient() {

    private lateinit var sslSocket: SSLSocket
    override val port: Int = 587

    override fun connect(smtpServer: String, timeout: Duration) {
        super.connect(smtpServer, timeout)
        socket.expectedResponse(250, "HELO alice")
        socket.expectedResponse(220, "STARTTLS")
        System.setProperty("javax.net.ssl.trustStore", "C:\\env\\JDKs\\openjdk-20.0.1\\lib\\security\\cacerts")
        val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        sslSocket = sslFactory.createSocket(socket, socket.inetAddress.hostAddress, socket.port, true) as SSLSocket
        sslSocket.useClientMode = true
        sslSocket.enableSessionCreation = true
        println("Start SSL")
        sslSocket.startHandshake()
        println("SSL done")
    }

    override fun <T> send(buildMessage: Message.() -> T) {
        require(isConnected) { "Missed connection" }
        with(sslSocket) {
            val message = Message().apply { buildMessage() }
            expectedResponse(334, "AUTH LOGIN")
            println("Waiting for login:")
            expectedResponse(334, readlnOrNull() ?: "anonymous")
            println("Waiting for password:")
            expectedResponse(235, readlnOrNull() ?: "anonymous")
            expectedResponse(250, "MAIL FROM: <${message.addressFrom}>")
            expectedResponse(250, "RCPT TO: <${message.addressTo}>")
            expectedResponse(354, "DATA")
            expectedResponse(250, message.content)
            expectedResponse(221, "QUIT")
        }
    }

    override fun close() {
        sslSocket.close()
        super.close()
    }
}