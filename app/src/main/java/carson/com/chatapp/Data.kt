package carson.com.chatapp

import android.os.AsyncTask

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


    public fun checkIfDone() :Boolean{
        val bool = startAuthObject.status == AsyncTask.Status.FINISHED
        if(!bool)
            return false
        if(id == null || connectionKey == null){//import if they are null
            val pair = startAuthObject.get()!!
            this.id = pair.first
            this.connectionKey = pair.second
        }
        return true
    }


    public fun hangTillReturn(timeout :Long = 10_000): Boolean {
        val startMs = System.currentTimeMillis()
        while(!checkIfDone() && startMs + timeout > System.currentTimeMillis()){
            Thread.sleep(10)
        }
        return checkIfDone()
    }




    private var startAuthObject = restartAuth()
    private fun restartAuth() :AsyncGetEncryptionKey{
        startAuthObject = AsyncGetEncryptionKey {
            data.restartAuth()
        }
        return startAuthObject
    }

    constructor(){
        startAuthObject.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }


}