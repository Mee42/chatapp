package carson.com.chatapp.activities

import android.accounts.NetworkErrorException
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView

import carson.com.chatapp.*
import carson.com.utils.hash

import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {

    var mAuthTask :UserLoginTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        email_sign_in_button.setOnClickListener { attemptLogin() }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if(mAuthTask != null)
            return
        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            println("starting background")
            showProgress(true)
            mAuthTask = UserLoginTask(emailStr, passwordStr)
            mAuthTask!!.execute()
        }
    }

    private fun isEmailValid(email: String): Boolean = true//TODO

    private fun isPasswordValid(password: String): Boolean = true//TODO

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinnerexecuteOnExecutor.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
            .setDuration(shortAnimTime)
            .alpha((if (show) 0 else 1).toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    login_form.visibility = if (show) View.GONE else View.VISIBLE
                }
            })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
            .setDuration(shortAnimTime)
            .alpha((if (show) 1 else 0).toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    login_progress.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
    }

    enum class Thing{
        USERNAME,PASSWORD
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val username: String, private val mPassword: String) :
        AsyncTask<Void, Void, Pair<Boolean, Pair<Thing,String>>>() {
        override fun doInBackground(vararg params: Void?): Pair<Boolean, Pair<Thing,String>> {
//            println("starting login task")
            if (!data.checkIfDone()) {
//                println("is not done")
                //is not done
                if(!data.hangTillReturn(10 * 1000))
                    throw NetworkErrorException("Timed out on start get")
            }
            //we can now assume that it has complected
            //check to see if the user exists
            //should block
            val checkIfExists = String(AsyncPost().doInBackground("/account/exists/$username"))
            if (checkIfExists == "true" || checkIfExists == "false") {
                if (checkIfExists == "false") {
                    return Pair(false, Pair(Thing.USERNAME,"user not found"))
                }//else, continue the program
            } else {
                throw NetworkErrorException("Could not check if user exists, got:$checkIfExists")
            }
            //get salt
            val salt = AsyncEncryptedPost(key = data.getKey()).doInBackground("/account/salt/${data.id}/$username")
            val hash = (mPassword.toByteArray() + salt).hash()
            val attemptLogin = AsyncEncryptedPost(hash, data.getKey())
            val string = String(attemptLogin.doInBackground("/account/check/${data.id}/$username"))
//            val string = String(attemptLogin.get(10, TimeUnit.SECONDS))
            if (!(string == "true" || string == "false")) {
                throw NetworkErrorException("Could not check if user exists, got:$checkIfExists")
            }
            if(string == "true"){
                return Pair(true, Pair(Thing.PASSWORD,"success"))
            }else{
                return Pair(false, Pair(Thing.PASSWORD,"incorrect password"))
            }
        }

        override fun onPostExecute(pair: Pair<Boolean, Pair<Thing,String>>?) {
            mAuthTask = null
            showProgress(false)
            if(pair != null) {
                if(!pair.first){
                    if(pair.second.first == Thing.USERNAME)
                        email.error = pair.second.second
                    else
                        password.error = pair.second.second
                }else {
                    val intent = Intent(baseContext,TestLoggedInActivity::class.java)
                    startActivity(intent)
                }
            }
//            println("onPostExecute:${pair?.first}  :  ${pair?.second}")
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
//            println("onCancelled")
        }
    }

}
