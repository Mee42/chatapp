package carson.com.server

import com.mongodb.client.MongoClients
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import spark.Spark.halt
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

//singlton
private var mongo :MongoClient? = null
public fun getMongoClient() :MongoClient{
    if(mongo === null)
        mongo = getClient()
    return mongo!!
}



//interface for getting immutable data from the database
fun getUser(username :String) :User{

}