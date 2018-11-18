package carson.com.utils

import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

fun String.hash() :String{
    val messageDigest = MessageDigest.getInstance("SHA-256")
    messageDigest.update(this.toByteArray())
    return String(messageDigest.digest())
}

fun ByteArray.combine(arr :ByteArray) :ByteArray{
    if(arr.size != this.size)
        throw IndexOutOfBoundsException("Attempting to combine an array of ${this.size} bytes and ${arr.size}")
    var index = 0
//    return mapInPlace { it.xor(arr[index++]) }
    return this.xor(arr)
//  return this
}

fun main(args: Array<String>) {
    val random = Random()//change to SecureRandom
    val test = ByteArray(8) { 0 }//Should be the same size as the key
    val key = ByteArray(32) { 0 }//Should be the same size as the key
    random.nextBytes(test)
    random.nextBytes(key)
    val result = test.AESencrypt(key).AESdecrypt(key)
    println(result.contentEquals(test) && test.contentEquals(result))
}


//inline fun ByteArray.mapInPlace(mutator: (Byte)->Byte) : ByteArray {
//    this.forEachIndexed { idx, value ->
//        mutator(value).let { newValue ->
//            if (value != newValue) this[idx] = mutator(value)
//        }
//    }
//    return this
//}
inline fun ByteArray.mapInPlace(mutator: (Byte) ->Byte) :ByteArray{
    val copy = this.copyOf()
    for(i in 0 until this.size)
        copy[i] = mutator.invoke(this[i])
    return copy
}

inline fun ByteArray.xor(other :ByteArray) :ByteArray {
    if(other.size != this.size)
        throw IndexOutOfBoundsException("Attempting to combine an array of ${this.size} bytes and ${other.size}")
    val copy = this.copyOf()
    for(i in 0 until this.size){
        copy[i] = this[i].xor(other[i])
    }
    return copy
}



fun ByteArray.decode(): String = this.fold("{") { all, byte-> "$all, $byte"}.replaceFirst(", ","") + "}"




const val ALGORITHM = "AES"
fun ByteArray.AESencrypt(key :ByteArray) :ByteArray {
    val secretKey = SecretKeySpec(key.copyOf(), ALGORITHM)
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    return cipher.doFinal(this.copyOf())
}

fun ByteArray.AESdecrypt(key :ByteArray) :ByteArray {
    val secretKey = SecretKeySpec(key.copyOf(), ALGORITHM)
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    return cipher.doFinal(this.copyOf())
}







