package carson.com.server



fun main(args: Array<String>) {
    val server = Server(9999)
    server.init()
    while(server.data.running){
        Thread.sleep(1000)
    }
}
