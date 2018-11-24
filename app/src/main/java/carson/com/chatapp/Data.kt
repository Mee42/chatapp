package carson.com.chatapp

import android.os.AsyncTask
import java.util.concurrent.TimeUnit

val data = Data()

/**
 * A singleton is kept in the above object. It should only be used per instance of server communication.
 * If the server does happen to disconnect during a client session, this object
 */
class Data {
    var connectionKey :ByteArray? = null
    var id :Int? = null

    fun getId():Int{
        if(id == null)
            throw IllegalAccessException("can not access ID before async task returns. Call Data#CheckIfDone to make sure it is done.")
        return id!!
    }

    fun getKey() :ByteArray{
        if(connectionKey == null)
            throw IllegalAccessException("can not access Key before async task returns. Call Data#CheckIfDone to make sure it is done.")
        return connectionKey!!
    }


    fun checkIfDone() :Boolean{
        val bool = startAuthObject.status == AsyncTask.Status.FINISHED
        if(!bool && startAuthObject.status != AsyncTask.Status.PENDING)
            return false
        if(id == null || connectionKey == null){//import if they are null
            val pair = startAuthObject.get()!!
            this.id = pair.first
            this.connectionKey = pair.second
        }
        return true
    }


    fun hangTillReturn(timeout :Long = 10_000): Boolean {
        startAuthObject.get(timeout,TimeUnit.MILLISECONDS)
        return checkIfDone()
    }




    private var startAuthObject = AsyncGetEncryptionKey()

    constructor(){
        startAuthObject.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }


}