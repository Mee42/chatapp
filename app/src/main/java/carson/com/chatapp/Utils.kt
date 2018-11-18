package carson.com.chatapp

import android.app.Activity
import android.support.annotation.IdRes
import android.view.View
import okhttp3.OkHttpClient
import java.util.*

fun <T : View> Activity.bind(@IdRes res : Int) : Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy { findViewById<T>(res) }
}


val client = OkHttpClient()
const val url = "http://192.168.1.202:9999/"
val random = Random()//change to SecureRandom