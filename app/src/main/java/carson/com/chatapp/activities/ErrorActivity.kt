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
import carson.com.utils.Logger
import carson.com.chatapp.R
import carson.com.chatapp.bind
import carson.com.chatapp.haste
import java.util.logging.Level
class ErrorActivity :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        Logger.fine("changed content view to error")
        val text : TextView by bind(R.id.error_text)
        text.text = Logger.getString(Level.ALL)
        Logger.finer("got logger text and displayed it")
        findViewById<Button>(R.id.error_copy).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText("Copied Text",text.text)
            Snackbar.make(findViewById(R.id.error_coordinatorLayout), "Copied", Snackbar.LENGTH_SHORT).show()
            Logger.fine("copy button clickd")
        }
        Logger.finer("set on-click listener")
    }

}


fun crash(op :AppCompatActivity) {
    Logger.info("CRASHING")
    op.startActivity(Intent(op, ErrorActivity::class.java))
 }