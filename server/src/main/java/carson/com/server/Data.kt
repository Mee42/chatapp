package carson.com.server

import carson.com.utils.hash
import carson.com.utils.hashString
import spark.Request
import spark.Spark
import java.lang.NumberFormatException
import java.util.*


class Data{
    val users = mutableListOf<User>()
    val protocolSessions = mutableListOf<ProtocolSession>()
    val sessions = mutableListOf<Session>()
    var running = true

    fun getUser(username :String) :User? = users.firstOrNull { it.username == username }

    fun getUser(req :Request) :User? = getUser(req.params("username"))


    /**
     * This is for any methods that will throw HaltException, for spark
     */
    inner class Sparky{
        fun getId(req : Request) :Int = getId(req.params("id"))
        fun getId(id :String) :Int {
            return try {
                Integer.parseInt(id)
            }catch(e : NumberFormatException){
                Spark.halt(400, "id is an invalid integer")
                -1
            }
        }

        fun getSession(id :Int):Session {
            try {
                return sessions.single { it.id == id }
            }catch (e : NoSuchElementException) {
                Spark.halt(400, "session not found")
            }catch(e :IllegalArgumentException){
                Spark.halt(400, "session not found")
            }
            return Session.NULL
        }

        fun getSession(req :Request) :Session {
            val id = getId(req)
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


}

class User {
    var username :String
    var passwordHash :ByteArray
    val passwordSalt :ByteArray

    constructor(username: String, passwordHash: ByteArray, passwordSalt: ByteArray) {
        this.username = username
        this.passwordHash = passwordHash
        this.passwordSalt = passwordSalt
    }

    constructor(username :String, password: ByteArray){
        this.username = username
        this.passwordSalt = ByteArray(128) {0}
        random.nextBytes(passwordSalt)
        this.passwordHash = (password + passwordSalt).hash()
    }
}

var index1 = 0
class ProtocolSession(val pad :ByteArray, val key :ByteArray, val id :Int)

class Session(val key :ByteArray,val id :Int){
    companion object {
        val NULL = Session(ByteArray(0){0},-1)
    }
}