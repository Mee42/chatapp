package carson.com.chatapp

import android.accounts.NetworkErrorException
import android.os.AsyncTask
import carson.com.chatapp.activities.crash
import carson.com.utils.AESdecrypt
import carson.com.utils.AESencrypt
import carson.com.utils.combine
import okhttp3.Request
import okhttp3.RequestBody


class AsyncGetEncryptionKey(val end :() -> Unit = {}) : AsyncTask<Unit, Unit, Pair<Int,ByteArray>>(){
    override fun doInBackground(vararg params: Unit?): Pair<Int,ByteArray> {
        val pad = ByteArray(keySize) { 0 }//Should be the same size as the key
        random.nextBytes(pad)
        //get an id
        val idReq = AsyncPost().doInBackground("/start/id")

        val id = Integer.parseInt(String(idReq))
        //gets S(k)
        var encryptedKey = AsyncPost().doInBackground("/start/$id/one")
        //turn into C(S(k))
        encryptedKey = encryptedKey.combine(pad)
        //send C(S(k)) to the server and get C(k)
        encryptedKey = AsyncPost(encryptedKey).doInBackground("/start/$id/two")

        val key = encryptedKey.combine(pad)
        //attempt to verify key

        val randomBytes = ByteArray(16) { 0 }
        val returnBytes = AsyncEncryptedPost(randomBytes, key).doInBackground("/start/$id/test")
        if (!(randomBytes + ByteArray(1) { 0 }).contentEquals(returnBytes)) {
            throw SecurityException("attempt to verify key failed")
        }
        return Pair(id, key)
    }

    override fun onPostExecute(result: Pair<Int, ByteArray>?) {
        super.onPostExecute(result)
        end.invoke()
    }
}

class AsyncEncryptedPost(private val body :ByteArray = ByteArray(0), val key :ByteArray) : AsyncTask<String, Void, ByteArray>(){
    public override fun doInBackground(vararg params: String?): ByteArray {
        val request = Request.Builder()
            .url(url + params[0])
            .post(RequestBody.create(null,body.AESencrypt(key)))
            .build()
        val response = client.newCall(request).execute()

//        println("response:${response.body()?.string()}")
        if(!response.isSuccessful)
            throw NetworkErrorException("got:${response.code()} body:${response.body()?.string()} at path: $url${params[0]}")
        val resultBody = response.body()?.bytes() ?: throw NetworkErrorException()
        return resultBody.AESdecrypt(key)
    }
}


class  AsyncPost(private val body :ByteArray = ByteArray(0)) : AsyncTask<String, Void, ByteArray>(){
    public override fun doInBackground(vararg params: String?): ByteArray {
        val request = Request.Builder()
            .url(url + params[0])
            .post(RequestBody.create(null,body))
            .build()
        val response = client.newCall(request).execute()
        return response.body()?.bytes() ?: throw NetworkErrorException()
    }
}
class  AsyncGet : AsyncTask<String, Void, ByteArray>(){
    public override fun doInBackground(vararg params: String?): ByteArray {
        val request = Request.Builder()
            .url(url + params[0])
            .get()
            .build()
        val response = client.newCall(request).execute()
        return response.body()?.bytes() ?: throw NetworkErrorException()
    }
}

var keySize = -1
get(){
    if(field == -1){
        return getKeySizeNetworked()
    }
    return field
}

class AsyncInitKey() :AsyncTask<Unit,Unit,Unit>(){
    override fun doInBackground(vararg params: Unit?){
        keySize
    }
}

fun getKeySizeNetworked() :Int {
    //println(("GETTING KEY")
    val request = Request.Builder()
        .url("$url/start/key_size")
        .get()
        .build()
    val execute = client.newCall(request).execute()
    val body = execute.body()?.string() ?: "*an invalid integer*"
    //println(("GOT KEY")
    val size = Integer.parseInt(body)
    keySize = size
    return size
}