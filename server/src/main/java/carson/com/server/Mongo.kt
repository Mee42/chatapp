package carson.com.server

import carson.com.utils.hash
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.*
import org.bson.Document
import org.bson.types.Binary
import spark.Request
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*


private fun getClient(name :String, password :String)= MongoClients.create(
    MongoClientSettings.builder()
        .applyToClusterSettings { builder -> builder.hosts(Arrays.asList(ServerAddress("192.168.1.203", 27017))) }
        .credential(MongoCredential.createCredential(name, "admin", password.toCharArray()))
        .build())

private val authFile : File = File("/etc/mon.conf")
private fun getAuth() : Pair<String,String>{
    val input = BufferedReader(FileReader(authFile))
    return Pair(input.readLine(),input.readLine())
}

private fun getClient() :MongoClient{
    val p = getAuth()
    return getClient(p.first,p.second)
}

//singleton
private var mongo :MongoClient? = null
fun getMongoClient() :MongoClient{
    if(mongo === null)
        mongo = getClient()
    return mongo!!
}

fun getDatabase() = getMongoClient().getDatabase("chat")

//interface for getting immutable data from the database
fun getUser(id :Long?) :User?{
    val db = getDatabase().getCollection("users")
    val doc = db[id ?: return null]
    return if(doc == null) null else User(doc)
}
fun getUser(req :Request) :User? = getUser(req.params("user_id")?.toLong())

fun createUser(username :String, email:String, password :ByteArray) :User{
    val passwordSalt = ByteArray(128) { 0 }
    random.nextBytes(passwordSalt)
    return User(generateId(),email,username, (password + passwordSalt).hash(), passwordSalt)
}

fun generateId(): Long {
    val etc = getDatabase().getCollection("etc")
    var doc = etc.find(all("_id", "snowflake")).first()
    if(doc == null) {
        doc = Document().append("_id", "snowflake").append("value", 10000L)
        etc.insertOne(doc)
    }
    val snowflake = doc["value"] as Long + 1
    doc.append("value",snowflake)
    etc.replaceOne(all("_id",doc["_id"]),doc)
    return snowflake
}

/**
 * Will return true if the user was updated, false if the user is new
 */
fun putOrReplaceUser(user :User) :Boolean{
    val users = getDatabase().getCollection("users")
    if(users.findOneAndReplace(all("_id",user.id),user.toDocument()) == null){
        //if it is null (user is new and not replaced), make a new one
        users.insertOne(user.toDocument())
        return false
    }
    return true
}

//methods making document usage easier
fun Document.getBytes(keyName :String) :ByteArray = (this[keyName] as Binary).data

operator fun MongoCollection<Document>.get(id :Any) :Document? = find(all("_id",id)).first()

fun Document.getID() :Any = this["_id"]!!
