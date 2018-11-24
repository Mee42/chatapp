package carson.com.chatapp

import android.os.AsyncTask
import carson.com.utils.Logger
import java.util.concurrent.TimeUnit

val data = Data()

/**
 * A loginActivityInstance is kept in the above object. It should only be used per instance of server communication.
 * If the server does happen to disconnect during a client session, this object
 */
class Data {
    var connectionKey :ByteArray? = null
    var id :Int? = null

    fun getId():Int{
        if(id == null) {
            Logger.info("can not access ID before async task returns. Call Data#CheckIfDone to make sure it is done.")

        }
        return id!!
    }

    fun getKey() :ByteArray{
        if(connectionKey == null) {
            Logger.info("can not access Key before async task returns. Call Data#CheckIfDone to make sure it is done.")
            hangTillReturn()
        }
        return connectionKey!!
    }


    fun checkIfDone() :Boolean{
        return id != null && connectionKey != null
    }


    fun hangTillReturn(timeout :Long = 10_000): Boolean {
        startAuthObject.get(timeout,TimeUnit.MILLISECONDS)
        return checkIfDone()
    }




    private var startAuthObject = AsyncGetEncryptionKey{key,id ->
        Logger.fine("got key, storing in data")
        data.connectionKey = key
        data.id = id
    }

    constructor(){
        startAuthObject.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }


}