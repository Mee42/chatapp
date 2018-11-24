package carson.com.server

import carson.com.utils.*
import com.mongodb.client.model.Filters
import spark.Spark.*
import java.lang.NumberFormatException
import java.util.*

val random = Random()//change to SecureRandom

const val size = 16

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
            get("/key_size"){_,_->
                println("returning size: $size")
                size
            }
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
                var id = data.Sparky().getId(req.params("id"))

                if(!data.protocolSessions.any { it.id == id }){
                    halt(400,"id not registered")
                }
                val session = data.protocolSessions.find { it.id == id }
                if(session == null) {
                    halt(500, "could not find session")
                }
                data.protocolSessions.removeIf { it == session }
                data.sessions+=Session(session!!.key, id)
                println("/:id/two called")
                req.bodyAsBytes().combine(session!!.pad)
            }//two
            post("/:id/test"){req, _ ->
                val session = data.Sparky().getSession(data.Sparky().getId(req))
                val bytes = req.bodyAsBytes().AESdecrypt(session.key)
                println("/:id/test called")
                (bytes + ByteArray(1) { 0 }).AESencryptChecked(session.key)
                return@post (bytes + ByteArray(1) { 0 }).AESencrypt(session.key)
            }//returns the decrypted body

        }//start

        path("/account") {

            get("/id_for_email/:email") {req,_ ->
                getDatabase().getCollection("users").find(Filters.all("email",req.params("username"))).toList()
                    .flatMap { listOf(it.getID()) }.fold("") {all,one-> "$all,$one"}.substring(1)//substring to remove the initial comma
            }
            get("/id_for_username/:username") {req,_ ->
                getDatabase().getCollection("users").find(Filters.all("username",req.params("username"))).toList()
                    .flatMap { listOf(it.getID()) }.fold("") {all,one-> "$all,$one"}.substring(1)//substring to remove the initial comma
            }


            get("/exists/:user_id") {req,_ ->
                getUser(req) != null
            }

            post("/salt/:id/:user_id"){req,_ ->
                val user = getUser(req)
                if(user == null) {
                    halt(400, "User not found");return@post ""
                }
                user.passwordSalt.AESencrypt(data.Sparky().getSession(req).key)
            }

            post("/check/:id/:user_id") {req,_ ->
                println("check:")
                var body = req.bodyAsBytes()
                val id = data.Sparky().getId(req)
                val session = data.Sparky().getSession(id)
                val user:User? = getUser(req)
                if(user == null){
                    halt(400,"could not find user with that username");
                }
                body = body.AESdecrypt(session.key)
                //the body should be the hashString
                if(body.contentEquals(user!!.passwordHash)){
                    return@post "true".toByteArray().AESencrypt(session.key)
                }
                return@post "false".toByteArray().AESencrypt(session.key)
            }
        }//account

    }//init

}