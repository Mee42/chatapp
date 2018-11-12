package carson.com.chatapp

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.widget.TextView
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.Random

val client = OkHttpClient()
val url = "http://192.168.1.202:9999/"
val random = Random()//change to SecureRandom

fun startTest(op: AppCompatActivity){
    val asyncTask = AsyncGetEncryptionKey().execute()

    op.findViewById<TextView>(R.id.test_raw).text = "loading..."
    op.findViewById<TextView>(R.id.test_raw).text = asyncTask.get().decode()
}



class AsyncGetEncryptionKey : AsyncTask<Unit,Int,ByteArray>(){
    override fun doInBackground(vararg params: Unit?): ByteArray {
        println("start main background")
        val pad = ByteArray(128) { 0 }//Should be the same size as the key
        random.nextBytes(pad)
        //get an id
        println("getting id")
        val idReq = AsyncPost().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"/start/id")
        val id = Integer.parseInt(idReq.get())
        println("got id:$id")
        //gets S(k)
        var encryptedKey = AsyncPost().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"/start/$id/one").get().toByteArray()
        println("got S(k):${encryptedKey.decode()}")
        //turn into C(S(k))
        encryptedKey = encryptedKey.combine(pad)
        println("made C(S(k)):${encryptedKey.decode()}")

        //send C(S(k)) to the server and get C(k)
        encryptedKey = AsyncPost(encryptedKey).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"/start/$id/two").get().toByteArray()
        println("got C(k):${encryptedKey.decode()}")

        return encryptedKey.combine(pad)
    }

}

fun ByteArray.decode() :String = Base64.encodeToString(this,0)


class  AsyncPost(private val body :ByteArray = ByteArray(0)) : AsyncTask<String,Void,String>(){
    override fun doInBackground(vararg params: String?): String {
        val request = Request.Builder()
            .url(url + params[0])
            .post(RequestBody.create(null,body))
            .build()
        val response = client.newCall(request).execute()
        return response.body()?.string() ?: "null"
    }
}