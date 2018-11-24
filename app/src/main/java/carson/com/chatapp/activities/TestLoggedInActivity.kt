package carson.com.chatapp.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import carson.com.chatapp.R

class TestLoggedInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testloggedin)
    }

    override fun onStart() {
        super.onStart()
        crash(this)
    }
}