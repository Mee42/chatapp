package carson.com.utils

import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

fun String.hashString() :String = String(hashBytes())

fun String.hashBytes() :ByteArray{
    val messageDigest = MessageDigest.getInstance("SHA-256")
    messageDigest.update(this.toByteArray())
    return messageDigest.digest()
}

fun ByteArray.hash() :ByteArray {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    messageDigest.update(this.copyOf())
    return messageDigest.digest()
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

fun ByteArray.xor(other :ByteArray) :ByteArray {
    if(other.size != this.size)
        throw IndexOutOfBoundsException("Attempting to combine an array of ${this.size} bytes and ${other.size}")
    val copy = this.copyOf()
    for(i in 0 until this.size){
        copy[i] = this[i].xor(other[i])
    }
    return copy
}



fun ByteArray.decode(): String = this.fold("{") { all, byte-> "$all, $byte"}.replaceFirst(", ","") + "}"




const val ALGORITHM = "AES/ECB/PKCS5PADDING"
const val KEY_ALGO = "AES"
//val IV :ByteArray = ByteArray(16) {0}



fun ByteArray.AESencrypt(key :ByteArray) :ByteArray {
    val secretKey = SecretKeySpec(key.copyOf(), KEY_ALGO)
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey/*, IvParameterSpec(IV)*/)
    return cipher.doFinal(this.copyOf())
}

fun ByteArray.AESencryptChecked(key :ByteArray) :ByteArray =  key.AESencrypt(key).AESdecrypt(key)

fun ByteArray.AESdecrypt(key :ByteArray) :ByteArray {
//    println("body:${String(this)}")
//    println("body:${decode()}")
//    println("key:${key.decode()}")
    val secretKey = SecretKeySpec(key.copyOf(), KEY_ALGO)
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.DECRYPT_MODE, secretKey/*,IvParameterSpec(IV)*/)
    val decrypted = cipher.doFinal(this.copyOf())
//    println("got:${String(decrypted)}  :  ${decrypted.decode()}")
    return decrypted
}


/*
    body:{-54, 27, -62, 122, 25, 110, 117, -96, -30, -81, -127, 111, 26, 47, -108, 124, 36, 97, 55, -34, 81, -15, -103, -49, 104, 54, 19, 54, 0, 39, 63, -15, -92, 110, -91, 78, 37, 117, 95, 37, -87, -23, -89, 40, -8, 41, 101, -59, 39, -122, 77, 122, 125, -71, 105, -39, -87, -12, 119, 64, -78, -118, -78, -96, 97, -21, 100, -95, 35, -9, 16, -127, -6, -116, 122, -56, -19, -25, 73, -13, -39, -60, 61, -4, 113, 70, -93, 48, 96, 101, 116, 14, 77, 84, 26, 59, 55, -119, 46, 56, 76, 61, -55, 72, -77, 38, -88, 113, 113, -89, -49, -5, -63, 124, 44, 8, 80, -8, -63, 2, 9, 19, -101, -100, 68, 76, 78, -97}
key:  :  {110, -55, -47, 87, -16, -44, -61, 51, -57, -51, -18, -41, -32, 57, -75, 85}
    body:{-63, -7, -109, -19, -77, 73, -18, 56, -78, -17, -34, 98, 74, 9, 50, -104, 35, 94, 22, 62, -106, -100, -72, -43, -123, -38, 7, 119, 39, 124, -93, -91}
key:  :  {110, -55, -47, 87, -16, -44, -61, 51, -57, -51, -18, -41, -32, 57, -75, 85}

 */




