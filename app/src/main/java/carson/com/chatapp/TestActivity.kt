package carson.com.chatapp

import android.accounts.NetworkErrorException
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import carson.com.utils.AESdecrypt
import carson.com.utils.AESencrypt
import carson.com.utils.combine
import carson.com.utils.decode
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.*
import java.util.concurrent.TimeUnit


val client = OkHttpClient()
const val url = "http://192.168.1.202:9999/"
val random = Random()//change to SecureRandom

fun startTest(op: AppCompatActivity){
    val asyncTask = AsyncGetEncryptionKey().execute()

    op.findViewById<TextView>(R.id.test_raw).text = "loading..."
    op.findViewById<TextView>(R.id.test_raw).text = asyncTask.get().decode()
}



class AsyncGetEncryptionKey : AsyncTask<Unit,Int,ByteArray>(){
    override fun doInBackground(vararg params: Unit?): ByteArray {
        val pad = ByteArray(keySize) { 0 }//Should be the same size as the key
        random.nextBytes(pad)
        //get an id
        val idReq = AsyncPost().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"/start/id")
        val id = Integer.parseInt(String(idReq.get()))
        //gets S(k)
        var encryptedKey = AsyncPost().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"/start/$id/one").get()
        //turn into C(S(k))
        encryptedKey = encryptedKey.combine(pad)
        //send C(S(k)) to the server and get C(k)
        encryptedKey = AsyncPost(encryptedKey).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"/start/$id/two").get()
        val key = encryptedKey.combine(pad)
        //attempt to verify key

        val randomBytes = ByteArray(2048) {Math.random().toByte()}
        val returnBytes = AsyncKeyPost(randomBytes,key).execute("/start/").get()
        val decryptedRandomBytes = randomBytes.AESdecrypt(key)
        if(!decryptedRandomBytes.contentEquals(returnBytes)){
            throw SecurityException("attempt to verify key failed")
        }
        return key
    }

}

class AsyncKeyPost(private val body :ByteArray = ByteArray(0), val key :ByteArray) : AsyncTask<String,Void,ByteArray>(){
    override fun doInBackground(vararg params: String?): ByteArray {
        val request = Request.Builder()
            .url(url + params[0])
            .post(RequestBody.create(null,body.AESencrypt(key)))
            .build()
        val response = client.newCall(request).execute()
        return response.body()?.bytes()?.AESdecrypt(key) ?: throw NetworkErrorException()
    }
}


class  AsyncPost(private val body :ByteArray = ByteArray(0)) : AsyncTask<String,Void,ByteArray>(){
    override fun doInBackground(vararg params: String?): ByteArray {
        val request = Request.Builder()
            .url(url + params[0])
            .post(RequestBody.create(null,body))
            .build()
        val response = client.newCall(request).execute()
//        println("body on ${params[0]} call:${response.body()?.string()}")
        return response.body()?.bytes() ?: throw NetworkErrorException()
    }
}

var keySize = -1
get(){
    if(field == -1){
        println("getting key size")
        field = AsyncGetKeySize().execute().get(5000, TimeUnit.MILLISECONDS) ?: throw NetworkErrorException("timed out on get")
    }
    return field
}

class AsyncGetKeySize : AsyncTask<Void,Void,Int>(){
    override fun doInBackground(vararg params: Void?): Int {
        println("request-1")
        val request = Request.Builder()
            .url("$url/start/key_size")
            .get()
            .build()
        println("request-2")
        val execute = client.newCall(request).execute()
        println("request-3")
        val body = execute.body()?.string() ?: "*an invalid integer*"
        println("request-4")
        return Integer.parseInt(body)
    }

}