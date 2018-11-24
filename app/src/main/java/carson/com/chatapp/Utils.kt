package carson.com.chatapp
import android.app.Activity
import android.os.AsyncTask
import android.support.annotation.IdRes
import android.view.View
import carson.com.utils.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.*


fun <T : View> Activity.bind(@IdRes res : Int) : Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(res) }
}


val client = OkHttpClient()
const val url = "http://192.168.1.202:9999"
val random = Random()//change to SecureRandom if need be (doubt)
