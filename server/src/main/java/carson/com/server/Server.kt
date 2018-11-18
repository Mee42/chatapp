package carson.com.server

import carson.com.utils.*
import spark.Spark.*
import java.lang.NumberFormatException
import java.util.*

val random = Random()//change to SecureRandom

const val size = 128

class Server (port :Int){
    val data = Data()
    init{
        port(port)
    }
    fun init(){
        get("/") {_,_ -> "Hello, World"}

        path("/start") {
            /*
            https://en.wikipedia.org/wiki/Three-pass_protocol
             */
            get("/key_size"){_,_-> size}
            post("/id") {_,_ ->
                println("giving id:$index1")
                return@post index1++
            }
            post("/:id/one") {req, _ ->//initiates the transaction. returns S(k)
                var id :Int? = null
                try {
                    id = Integer.parseInt(req.params("id"))
                }catch(e :NumberFormatException){
                    halt(400,"id is an invalid integer")
                }
                if(data.protocolSessions.any { id == it.id }){
                    halt(400,"id already taked")
                }
                val pad = ByteArray(size) { 0 }//Should be the same size as the key
                var key = ByteArray(size) { 0 }
                random.nextBytes(pad)
                random.nextBytes(key)
                println("generating key:${key.decode()}")

                val session = ProtocolSession(pad, key, id!!)
                data.protocolSessions.add(session)
                val post = key.combine(pad)
                post
            }//one
            post("/:id/two") {req,_ ->//gets C(S(k)), returns C(k)
                var id = data.getIdSpark(req.params("id"))

                if(!data.protocolSessions.any { it.id == id }){
                    halt(400,"id not registered")
                }
                val session = data.protocolSessions.find { it.id == id }
                if(session == null) {
                    halt(500, "could not find session")
                }
                data.protocolSessions.removeIf { it == session }
                data.sessions+=Session(session!!.key, id)
                req.bodyAsBytes().combine(session!!.pad)
            }//two
            post("/:id/test"){req, _ ->
                val session = data.getSessionSpark(req.params("id"))
                val bytes = req.bodyAsBytes()
                return@post bytes.AESdecrypt(session.key)
            }//returns the decrypted body

        }//start


        get("/test"){_,_->
            val id = 12;
            val session = data.protocolSessions.find { it.id == id }
            if(session == null) {
                halt(500, "could not find session")
            }
            return@get Session(session!!.key,id)
        }


    }//init

}


