fun main(args: Array<String>) {
//    run(arrayOf("smtp.gmail.com", "test1@gmail.com", "test2@gmail.com", "Help")) //failing
    run(arrayOf("ssl", "smtp.gmail.com", "test1@gmail.com", "test2@gmail.com", "Help")) //expect login password
}

fun run(args: Array<String>) {
    val isSsl = args.getOrNull(0)?.equals("ssl", ignoreCase = true) == true
    val arguments = if (isSsl) args.copyOfRange(1, args.lastIndex) else args
    val smtpClient = if (isSsl) SmtpClientSSL() else SmtpClient()
    smtpClient.use { client ->
        client.connect(arguments.getOrNull(0) ?: throw IllegalArgumentException("\"SMTP server\" in first arg expected"))
        client.send {
            this.addressFrom = arguments.getOrNull(1) ?: throw IllegalArgumentException("email \"FROM\" in second arg expected")
            this.addressTo = arguments.getOrNull(2) ?: throw IllegalArgumentException("email \"TO\" in third arg expected")
            this.content = arguments.drop(3).joinToString(" ")
        }
    }
}