package br.com.fclug.financialaid

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.com.fclug.financialaid.server.*
import br.com.fclug.financialaid.utils.TAG
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.ext.android.inject
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {

    private var disposable: Disposable? = null
    private val api: ServerApi by inject()

    private val loginWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            login_signin.isEnabled = login_username.text.toString().trim { it <= ' ' }.isNotEmpty()
                    && login_password.text.toString().trim { it <= ' ' }.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_username.addTextChangedListener(loginWatcher)
        login_password.addTextChangedListener(loginWatcher)


        val progressDialog = AlertDialog.Builder(this)
                .setView(R.layout.progress_dialog)
                .setTitle(resources.getString(R.string.login_dialog_title))
                .setCancelable(false)
                .create()

        val alertDialog = AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.login_failed))
                .create()
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK") { dialog, which ->
            login_username.text.clear()
            login_password.text.clear()
        }

        val sessionManager: SessionManager by inject()

        login_signin.setOnClickListener {

            val username = login_username.text.toString()
            val password = login_password.text.toString()
            val userLogin = UserLogin(username, password)

            progressDialog.show()
            disposable = api.login(userLogin)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({response ->
                        progressDialog.dismiss()

                        val name = response.result.name
                        val token = response.result.token

                        // Creating user login session
                        sessionManager.createLoginSession(username, name, password, token)
                        sessionManager.sendFbTokenToServer()
                        // Start MainActivity
                        val i = Intent(applicationContext, MainActivity::class.java)
                        startActivity(i)
                        finish()
                    }, {error ->
                        progressDialog.dismiss()

                        if(error is HttpException) {
                            error.response()?.errorBody()?.let {
                                val errorResponse = api
                                        .convertErrorResponseBody<ApiResponseError>(it)
                                errorResponse?.let { response ->
                                    alertDialog.setMessage(response.errors[0].description)
                                    alertDialog.show()
                                }
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, R.string.api_request_failed,
                                    Toast.LENGTH_SHORT).show()
                            Log.d(TAG, error.message)
                            error.printStackTrace()
                        }
                    })

        }

        login_signup.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        login_skip.setOnClickListener { sessionManager.skipLogin() }
    }
}