package carson.com.utils

import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level

/**
 * Static methods for loggging, so crash reports can be generated quickly without effort
 */
class Logger { companion object {
    private val logged = arrayListOf<Entry>()
    fun getString(max :Level = Level.ALL) :String{
        val str = StringBuilder()
        synchronized(logged) {
            for (log in logged) {
                if (log.level.intValue() >= max.intValue()) {
                    str.appendln(log.toString())
                }
            }
        }
        return str.toString()
    }

    private fun log(level :Level, string: String,exception: Throwable? = null){
        logged += Entry(level, string, exception)
    }

    /**
     * This is for anything system-breaking. NetworkErrorException are the one exception to this, and should be logged with Logger#network, and then handled
     * Ex: Exceptions thrown in places they shouldn't
     */
    fun severe(string: String,exception: Throwable? = null) =
        log(Level.SEVERE, string, exception)

    /**
     * This is for anything that may be a problem, but is recoverable.
     * Ex: A key becomes invalid
     */
    fun warning(string: String,exception: Throwable? = null) =
        log(Level.WARNING, string, exception)

    /**
     * For network stuff
     * Ex: Unable to connect the server
     */
    fun network(string: String = NETWORK_EXCEPTION,exception: Throwable? = null) =
        log(NetworkLevel.NETWORK, string, exception)
    /**
     * For problems cause by the user:
     * Ex: empty password field
     */
    fun info(string: String,exception: Throwable? = null) =
        log(Level.INFO, string, exception)

    /**
     * This is for testing purposes, and will be printed. These should only exist during testing
     * Ex: request body in String forms, when ByteArrays are going to be used
     */
    fun config(string: String,exception: Throwable? = null) =
        log(Level.CONFIG, string, exception)

    /**
     *  General logging
     *  Ex: when Button clicked
     */
    fun fine(string: String,exception: Throwable? = null) =
        log(Level.FINE, string, exception)

    /**
     * Finer logging for more mundane stuff
     * Ex: Button on-click handler set
     */
    fun finer(string: String,exception: Throwable? = null) =
        log(Level.FINER, string, exception)

    /**
     * For stuff that probably shouldn't be logged, but whatever,
     * Ex: I don't know
     */
    fun finest(string: String,exception: Throwable? = null) =
        log(Level.FINEST, string, exception)

    fun printAll() {
        println(toString())
    }




} }

data class Entry(val level :Level = Level.ALL,val message :String = "",val exception :Throwable? = null) {
    override fun toString(): String {
        val timestamp = SimpleDateFormat("HH.mm.ss",Locale.US).format(Date())
        return "$timestamp--$level : $message" + (if(exception != null) "\n" + exception.getEverything() else "")
    }
}

private fun Throwable.getEverything(): String {
    val writer = StringWriter()
    val printWriter = PrintWriter( writer )
    printStackTrace( printWriter )
    printWriter.flush()
    return writer.toString()
}

const val NETWORK_EXCEPTION = "Unable to connect to the server. This may be a problem with your internet, or the server might be down"

