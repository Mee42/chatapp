package carson.com.server

import carson.com.utils.*
import spark.ResponseTransformer
import spark.Spark.*
import java.lang.NumberFormatException
import java.math.BigInteger
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
                var id :Int?
                try {
                    id = Integer.parseInt(req.params("id"))
                }catch(e :NumberFormatException){
                    halt(400,"id is an invalid integer")
                    return@post ""//will never run
                }
                if(data.protocolSessions.any { it.id == id }){
                    halt(400,"id already taked")
                }
                val pad = ByteArray(size) { 0 }//Should be the same size as the key
                var key = ByteArray(size) { 0 }
                random.nextBytes(pad)
                random.nextBytes(key)
                println("generating key:${key.decode()}")

                val session = ProtocolSession(pad, key, id)
                data.protocolSessions.add(session)
                val post = key.combine(pad)
                post
            }//one
            post("/:id/two") {req,_ ->//gets C(S(k)), returns C(k)
                var id = data.getId(req.params("id"))

                if(!data.protocolSessions.any { it.id == id }){
                    halt(400,"id not registered")
                }
                val session = data.protocolSessions.find { it.id == id }
                if(session == null) {
                    halt(500, "could not find session")
                    return@post ""
                }
                data.protocolSessions.removeIf { it == session }
                data.sessions+=Session(session.key, id!!)
                req.bodyAsBytes().combine(session.pad)
            }//two
            post("/:id/test"){req, _ ->
                val session = data.getSession(req.params("id"))
                val bytes = req.bodyAsBytes()
                return@post bytes.AESdecrypt(session.key)
            }//returns the decrypted body

        }//start

        path("/account"){





        }

    }

}

class Decoder :ResponseTransformer {
    override fun render(model: Any?): String {
        return model.toString()
    }
}


class Data(){
    val users = mutableListOf<User>()
    val protocolSessions = mutableListOf<ProtocolSession>()
    val sessions = mutableListOf<Session>()
    var running = true
    fun getId(id :String) :Int{
        return try {
            Integer.parseInt(id)
        }catch(e :NumberFormatException){
            halt(400,"id is an invalid integer")
            -1
        }
    }
    fun getSession(id :String) :Session{
        return getSession(getId(id))
    }
    fun getSession(id :Int):Session{
        try {
            return sessions.single { it.id == id }
        }catch (e :NoSuchElementException) {
            halt(400,"session not found")
        }catch(e :IllegalArgumentException){
            halt(400,"session not found")
        }
        return Session.NULL
    }
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

class Session(val key :ByteArray,val id :Int){
    companion object {
        val NULL = Session(ByteArray(0){0},-1)
    }
}