package carson.com.server

import java.util.*
import spark.Spark.*
import java.lang.NumberFormatException
import java.nio.charset.Charset
import javax.crypto.KeyGenerator

val random = Random()//change to SecureRandom


class Server (port :Int){
    val data = Data()
    init{
        port(port)
    }
    fun init(){
        get("/") {_,_ -> "Hello, World"}

        path("/start") {
            post("/id") {_,_ ->
                println("got id:$index1")
                return@post index1++
            }
            post("/:id/one") {req, _ ->//initiates the transaction. returns S(k)
                var id :Int?
                try {
                    id = Integer.parseInt(req.params("id"))
                }catch(e :NumberFormatException){
                    return@post "400:id an invalid integer"
                }
                if(data.protocolSessions.any { it.id == id }){
                    return@post "400:id already registered"
                }
                val pad = ByteArray(128) { 0 }//Should be the same size as the key
                random.nextBytes(pad)
                var key = ByteArray(128) { 0 }
                random.nextBytes(key)
                key = Base64.getDecoder()
                println("key:${String(key)}")
                val session = ProtocolSession(pad, key, id)
                return@post key.combine(pad)
            }//one

            post("/:id/two") {req,_ ->//gets C(S(k)), returns C(k)
                var id :Int?
                try {
                    id = Integer.parseInt(req.params("id"))
                }catch(e :NumberFormatException){
                    return@post "400:id an invalid integer"
                }
                if(!data.protocolSessions.any { it.id == id }){
                    return@post "400:id not registered"
                }
                val session = data.protocolSessions.find { it.id == id } ?: return@post "500:could not find session"
                data.protocolSessions.removeIf { it == session }
                return@post req.bodyAsBytes().combine(session.key)
            }


        }//start

    }

}

class Data(){
    val users = mutableListOf<User>()
    val protocolSessions = mutableListOf<ProtocolSession>()
    var running = true;

}

class User {
    var username :String
    var passwordHash :String
    val passwordSalt :String

    constructor(username: String, passwordHash: String, passwordSalt: String) {
        this.username = username
        this.passwordHash = passwordHash
        this.passwordSalt = passwordSalt
    }

    constructor(username :String, password: String){
        this.username = username
        this.passwordSalt = "${UUID.randomUUID().toString().hash().hashCode()}"
        this.passwordHash = (password.hash() + passwordSalt.hash()).hash()
    }

    fun verifyPassword(password :String) :Boolean = (password.hash() + passwordSalt.hash()).hash() == passwordHash
}

var index1 = 0
class ProtocolSession(val pad :ByteArray, val key :ByteArray, val id :Int)

