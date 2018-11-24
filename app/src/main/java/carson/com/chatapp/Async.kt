package carson.com.chatapp

import android.accounts.NetworkErrorException
import android.os.AsyncTask
import carson.com.chatapp.activities.crash
import carson.com.chatapp.activities.loginActivityInstance
import carson.com.utils.*
import okhttp3.Request
import okhttp3.RequestBody


class AsyncGetEncryptionKey(val end :(ByteArray,Int) -> Unit = {_,_->}) : AsyncTask<Unit, Unit, Pair<Int,ByteArray>>(){
    override fun doInBackground(vararg params: Unit?): Pair<Int,ByteArray> {
        val pad = ByteArray(keySize) { 0 }//Should be the same size as the key
        random.nextBytes(pad)
        Logger.finer("generated pad")
        //get an id
        val idReq = AsyncPost().doInBackground("/start/id")
        Logger.finer("get id:$idReq")
        val id = Integer.parseInt(String(idReq))
        //gets S(k)

        var encryptedKey = AsyncPost().doInBackground("/start/$id/one")
        Logger.finer("S(k)${parseBytes(encryptedKey)}")
        //turn into C(S(k))
        encryptedKey = encryptedKey.combine(pad)
        Logger.finer("C(S(k))${parseBytes(encryptedKey)}")
        //send C(S(k)) to the server and get C(k)
        encryptedKey = AsyncPost(encryptedKey).doInBackground("/start/$id/two")
        Logger.finer("C(k)${parseBytes(encryptedKey)}")

        val key = encryptedKey.combine(pad)
        Logger.finer("Key:${parseBytes(key)}")
        //attempt to verify key

        val randomBytes = ByteArray(16) { 0 }
        val returnBytes = AsyncEncryptedPost(randomBytes, key).doInBackground("/start/$id/test")
        Logger.fine("got 17 0's :${parseBytes(encryptedKey)}")
        if (!(randomBytes + ByteArray(1) { 0 }).contentEquals(returnBytes)) {
            Logger.severe("attempt to verify key failed",SecurityException("attempt to verify key failed"))
            crash(loginActivityInstance)
        }
        Logger.fine("got key:${key.decode()}")
        return Pair(id, key)
    }

    override fun onPostExecute(result: Pair<Int, ByteArray>?) {
        super.onPostExecute(result)
        if(result != null)
            end.invoke(result.second,result.first)
        else
            Logger.warning("result is null",RuntimeException())
    }
}

class AsyncEncryptedPost(private val body :ByteArray = ByteArray(0), val key :ByteArray) : AsyncTask<String, Void, ByteArray>(){
    public override fun doInBackground(vararg params: String?): ByteArray {
        Logger.fine("starting encrypted post request at path ${params[0]}")
        val request = Request.Builder()
            .url(url + params[0])
            .post(RequestBody.create(null,body.AESencrypt(key)))
            .build()
        val response = client.newCall(request).execute()

//        println("response:${response.body()?.string()}")
        val bytes = response.body()?.bytes()
        Logger.finer("got " + parseBytes(bytes) + "at path ${params[0]}")
        return bytes?.AESdecrypt(key) ?: throw NetworkErrorException()
    }
}


class  AsyncPost(private val body :ByteArray = ByteArray(0)) : AsyncTask<String, Void, ByteArray>(){
    public override fun doInBackground(vararg params: String?): ByteArray {
        Logger.fine("starting post request at path ${params[0]}")

        val request = Request.Builder()
            .url(url + params[0])
            .post(RequestBody.create(null,body))
            .build()
        val response = client.newCall(request).execute()
        val bytes = response.body()?.bytes()
        Logger.finer("got " + parseBytes(bytes) + "at path ${params[0]}")
        return bytes ?: throw NetworkErrorException()
    }
}

class  AsyncGet : AsyncTask<String, Void, ByteArray>(){
    public override fun doInBackground(vararg params: String?): ByteArray {
        Logger.fine("starting get request at path $url${params[0]}")
        val request = Request.Builder()
            .url(url + params[0])
            .get()
            .build()
        val response = client.newCall(request).execute()
        val bytes = response.body()?.bytes()
        Logger.finer("got " + parseBytes(bytes) + "at path $url${params[0]}")
        return bytes ?: throw NetworkErrorException()
    }
}

fun parseBytesOrNothing(bytes :ByteArray?):String {
    return when {
        bytes == null -> "null"
        isUTF8(bytes) -> String(bytes)
        else -> "none-printable"
    }
}

fun isUTF8(pText: ByteArray): Boolean {

    var expectedLength :Int

    var i = 0
    while (i < pText.size) {
        expectedLength = when {
            pText[i].toInt() and 128 == 0 -> 1
            pText[i].toInt() and 224 == 192 -> 2
            pText[i].toInt() and 240 == 224 -> 3
            pText[i].toInt() and 248 == 240 -> 4
            pText[i].toInt() and 252 == 248 -> 5
            pText[i].toInt() and 254 == 252 -> 6
            else -> return false
        }

        while (--expectedLength > 0) {
            if (++i >= pText.size)
                return false
            if (pText[i].toInt() and 192 != 128)
                return false
        }
        i++
    }

    return true
}

fun parseBytes(bytes :ByteArray?) :String{
    return parseBytesOrNothing(bytes) + " : " + bytes?.decode()
}

var keySize = -1
get(){
    if(field == -1){
        return getKeySizeNetworked()
    }
    return field
}

fun getKeySizeNetworked() :Int {
    Logger.fine("getting key size")
    val request = Request.Builder()
        .url("$url/start/key_size")
        .get()
        .build()
    val execute = client.newCall(request).execute()
    val body = execute.body()?.string() ?: "*an invalid integer*"
    val size = Integer.parseInt(body)
    Logger.fine("key size:$size")
    keySize = size
    return size
}
