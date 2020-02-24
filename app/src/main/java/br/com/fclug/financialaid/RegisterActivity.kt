package br.com.fclug.financialaid

import android.content.DialogInterface
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.com.fclug.financialaid.server.*
import br.com.fclug.financialaid.utils.TAG
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.register_activity.*
import org.koin.android.ext.android.inject
import retrofit2.HttpException

class RegisterActivity : AppCompatActivity() {

    private var disposable: Disposable? = null
    private val api: ServerApi by inject()

    private val usernameWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(s.toString().isNotEmpty()) {
                disposable = api.checkUsername(s.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            val exists = response.result.existing == 1
                            if (exists) {
                                register_feedback.setImageResource(android.R.drawable.ic_delete)
                            } else {
                                ContextCompat.getDrawable(applicationContext,
                                        R.drawable.ic_confirm)?.apply {
                                    this.setColorFilter(ContextCompat.getColor(applicationContext,
                                            R.color.transaction_type_credit), PorterDuff.Mode.SRC_ATOP)
                                    register_feedback.setImageDrawable(this)
                                }
                            }
                            register_feedback.visibility = View.VISIBLE
                        }, { error ->
                            Toast.makeText(this@RegisterActivity, R.string.api_request_failed,
                                    Toast.LENGTH_SHORT).show()
                            Log.d(TAG, error.message)
                            error.printStackTrace()
                        })
            } else {
                register_feedback.visibility = View.GONE
            }
        }
    }

    private val registerEnableWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val username = register_username.text.toString().trim { it <= ' ' }
            val name = register_name.text.toString().trim { it <= ' ' }
            val password = register_password.text.toString()
            val passwordRepeat = register_password_repeat.text.toString()

            val passwordInputted = password.isNotEmpty() && passwordRepeat.isNotEmpty()
            val passwordValid = password == passwordRepeat

            register_password_alert.visibility = if(passwordInputted && !passwordValid)
                View.VISIBLE else View.GONE


            //TODO: add minimum size and complexity rules
            register_button.isEnabled = name.isNotEmpty() && username.isNotEmpty() &&
                    passwordInputted && passwordValid
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        register_name.addTextChangedListener(registerEnableWatcher)
        register_username.addTextChangedListener(registerEnableWatcher)
        register_username.addTextChangedListener(usernameWatcher)
        register_password.addTextChangedListener(registerEnableWatcher)
        register_password_repeat.addTextChangedListener(registerEnableWatcher)

        val progressDialog = AlertDialog.Builder(this)
                .setView(R.layout.progress_dialog)
                .setTitle(resources.getString(R.string.register_dialog_title))
                .setCancelable(false)
                .create()

        val alertDialog = AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.registration_failed))
                .create()
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK") { _, _ ->
            register_username.text.clear()
            register_password.text.clear()
            register_password_repeat.text.clear()
        }
        register_button.setOnClickListener {
            val username = register_username.text.toString().trim { it <= ' ' }
            val name = register_name.text.toString().trim { it <= ' ' }
            val password = register_password.text.toString()
            val userCreation = UserCreation(username, name, password)

            progressDialog.show()
            disposable = api.register(userCreation)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({response ->
                        progressDialog.dismiss()
                        Log.d(TAG, response.result.toString())
                        val sessionManager : SessionManager by inject()
                        sessionManager.checkLogin()
                    }, {error ->
                        progressDialog.dismiss()

                        if(error is HttpException) {
                            error.response()?.errorBody()?.let {
                                val errorResponse = api.convertErrorResponseBody<ApiResponseError>(it)
                                errorResponse?.let { response ->
                                    alertDialog.setMessage(response.errors[0].description)
                                    alertDialog.show()
                                }
                            }
                        } else {
                            Toast.makeText(this@RegisterActivity, R.string.api_request_failed,
                                    Toast.LENGTH_SHORT).show()
                            Log.d(TAG, error.message)
                            error.printStackTrace()
                        }
                    })
        }
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }
}