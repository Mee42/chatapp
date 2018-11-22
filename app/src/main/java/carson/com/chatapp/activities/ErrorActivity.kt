package carson.com.chatapp.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import carson.com.chatapp.R
import carson.com.chatapp.bind
import java.io.PrintWriter
import java.io.StringWriter

const val ERROR_MESSAGE = "carson.com.chatapp.activities.ErrorActivity.ERROR_MESSAGE"


class ErrorActivity :AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        val text : TextView by bind(R.id.error_text)
        text.text = intent.getStringExtra(ERROR_MESSAGE)

        findViewById<Button>(R.id.error_copy).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText("Copied Text",text.text)
            Snackbar.make(findViewById(R.id.error_coordinatorLayout), "Copied", Snackbar.LENGTH_SHORT).show()
        }
    }

}

fun crash(op: AppCompatActivity, error: String,exception: Exception? = null) {
    var exception = exception
    var error = error
    if(exception != null) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        error = sw.toString() + "\n\n\n$error" // stack trace as a string
    }
    val intent = Intent(op, ErrorActivity::class.java).apply { putExtra(ERROR_MESSAGE, error) }
    op.startActivity(intent)
}