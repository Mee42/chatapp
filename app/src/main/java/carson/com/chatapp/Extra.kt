package carson.com.chatapp

import java.security.MessageDigest
import kotlin.experimental.xor

fun String.hash() :String{
    val messageDigest = MessageDigest.getInstance("SHA-256")
    messageDigest.update(this.toByteArray())
    return String(messageDigest.digest())
}

fun ByteArray.combine(arr :ByteArray) :ByteArray{
    var index = 0
    return mapInPlace { it.xor(arr[index++ % arr.size]) }
}

inline fun ByteArray.mapInPlace(mutator: (Byte)->Byte) : ByteArray {
    this.forEachIndexed { idx, value ->
        mutator(value).let { newValue ->
            if (value != newValue) this[idx] = mutator(value)
        }
    }
    return this
}
