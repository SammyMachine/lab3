import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

internal fun Socket.expectedResponse(message: String? = null, expected: BufferedReader.() -> Boolean, error: () -> String) {
    val reader = BufferedReader(InputStreamReader(inputStream))
    val writer = PrintWriter(OutputStreamWriter(outputStream), true)
    message?.let { msg -> writer.println(msg) }
    if (!expected(reader)) throw IllegalStateException("Unexpected server response! ${error()}")
}

internal fun Socket.expectedResponse(expectedCode: Int, message: String? = null) {
    var error = "No response"
    this@expectedResponse.expectedResponse(message, expected = lam@{
        val line = readLine() ?: return@lam false
        println("\n###Response###\n$line\n")
        error = "Received $line instead of $expectedCode code"
        line.startsWith((expectedCode.toString()))
    },
        error = { error }
    )
}

data class Message(
    var addressFrom: String = "test1@gmail.com",
    var addressTo: String = "test2@gmail.com",
    var content: String = ""
)