package carson.com.server

import carson.com.utils.hash
import spark.Spark
import java.lang.NumberFormatException
import java.util.*


class Data{
    val users = mutableListOf<User>()
    val protocolSessions = mutableListOf<ProtocolSession>()
    val sessions = mutableListOf<Session>()
    var running = true
    fun getIdSpark(id :String) :Int{
        return try {
            Integer.parseInt(id)
        }catch(e : NumberFormatException){
            Spark.halt(400, "id is an invalid integer")
            -1
        }
    }
    fun getSessionSpark(id :String) :Session{
        return getSessionSpark(getIdSpark(id))
    }
    private fun getSessionSpark(id :Int):Session{
        try {
            return sessions.single { it.id == id }
        }catch (e : NoSuchElementException) {
            Spark.halt(400, "session not found")
        }catch(e :IllegalArgumentException){
            Spark.halt(400, "session not found")
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