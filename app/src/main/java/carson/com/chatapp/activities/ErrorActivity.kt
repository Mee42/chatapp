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
import carson.com.chatapp.Logger
import carson.com.chatapp.R
import carson.com.chatapp.bind
import java.util.logging.Level
class ErrorActivity :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        val text : TextView by bind(R.id.error_text)
        text.text = Logger.getString(Level.ALL)

        findViewById<Button>(R.id.error_copy).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText("Copied Text",text.text)
            Snackbar.make(findViewById(R.id.error_coordinatorLayout), "Copied", Snackbar.LENGTH_SHORT).show()
        }
    }

}

fun crash(op :AppCompatActivity) {
    op.startActivity(Intent(op, ErrorActivity::class.java))
 }