package carson.com.server

import carson.com.utils.hash
import carson.com.utils.hashString
import org.bson.Document
import spark.Request
import spark.Spark
import java.lang.NumberFormatException
import java.util.*


class Data{
    val users = mutableListOf<User>()
    val protocolSessions = mutableListOf<ProtocolSession>()
    val sessions = mutableListOf<Session>()
    var running = true



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

data class User(val username: String,val passwordHash: ByteArray,val passwordSalt: ByteArray) {

    constructor(doc: Document) : this(doc["_id"] as String, doc.getBytes("hash"), doc.getBytes("salt"))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (username != other.username) return false
        if (!passwordHash.contentEquals(other.passwordHash)) return false
        if (!passwordSalt.contentEquals(other.passwordSalt)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + passwordHash.contentHashCode()
        result = 31 * result + passwordSalt.contentHashCode()
        return result
    }

    fun toDocument(): Document {
        return Document()
            .append("_id",username)
            .append("hash",passwordHash)
            .append("salt",passwordSalt)
    }
}

var index1 = 0
class ProtocolSession(val pad :ByteArray, val key :ByteArray, val id :Int)

class Session(val key :ByteArray,val id :Int){
    companion object {
        val NULL = Session(ByteArray(0){0},-1)
    }
}