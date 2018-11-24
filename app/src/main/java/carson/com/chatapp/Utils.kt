package carson.com.chatapp
import android.app.Activity
import android.support.annotation.IdRes
import android.view.View
import okhttp3.OkHttpClient
import java.util.*
import java.util.logging.Logger


fun <T : View> Activity.bind(@IdRes res : Int) : Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(res) }
}


val client = OkHttpClient()
const val url = "http://192.168.1.202:9999"
val random = Random()//change to SecureRandom


const val NETWORK_EXCEPTION = "Unable to connect to the server. This may be a problem with your internet, or the server might be down"

