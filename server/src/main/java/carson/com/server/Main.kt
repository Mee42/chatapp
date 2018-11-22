package carson.com.server

import org.slf4j.LoggerFactory
import sun.util.logging.LoggingSupport.setLevel
import org.slf4j.Logger.ROOT_LOGGER_NAME





fun main(args: Array<String>) {

//    val rootLogger =LoggerFactory.getLogger(Server::class.java)


    val server = Server(9999)
    server.init()
    while(server.data.running){
        Thread.sleep(1000)
    }
}
