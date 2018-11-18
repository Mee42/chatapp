package carson.com.chatapp

import android.accounts.NetworkErrorException
import android.os.AsyncTask
import carson.com.utils.AESdecrypt
import carson.com.utils.AESencrypt
import carson.com.utils.combine
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit


class AsyncGetEncryptionKey : AsyncTask<Unit, Int, ByteArray>(){
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
        val returnBytes = AsyncEncryptedPost(randomBytes,key).execute("/start/").get()
        val decryptedRandomBytes = randomBytes.AESdecrypt(key)
        if(!decryptedRandomBytes.contentEquals(returnBytes)){
            throw SecurityException("attempt to verify key failed")
        }
        return key
    }

}

class AsyncEncryptedPost(private val body :ByteArray = ByteArray(0), val key :ByteArray) : AsyncTask<String, Void, ByteArray>(){
    override fun doInBackground(vararg params: String?): ByteArray {
        val request = Request.Builder()
            .url(url + params[0])
            .post(RequestBody.create(null,body.AESencrypt(key)))
            .build()
        val response = client.newCall(request).execute()
        return response.body()?.bytes()?.AESdecrypt(key) ?: throw NetworkErrorException()
    }
}


class  AsyncPost(private val body :ByteArray = ByteArray(0)) : AsyncTask<String, Void, ByteArray>(){
    override fun doInBackground(vararg params: String?): ByteArray {
        val request = Request.Builder()
            .url(url + params[0])
            .post(RequestBody.create(null,body))
            .build()
        val response = client.newCall(request).execute()
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

class AsyncGetKeySize : AsyncTask<Void, Void, Int>(){
    override fun doInBackground(vararg params: Void?): Int {
        val request = Request.Builder()
            .url("$url/start/key_size")
            .get()
            .build()
        val execute = client.newCall(request).execute()
        val body = execute.body()?.string() ?: "*an invalid integer*"
        return Integer.parseInt(body)
    }

}