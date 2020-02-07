package br.com.fclug.financialaid

import android.app.ProgressDialog
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
import br.com.fclug.financialaid.server.ApiRequest
import br.com.fclug.financialaid.server.ApiRequest.RequestCallback
import br.com.fclug.financialaid.server.ServerApi
import br.com.fclug.financialaid.server.ServerUtils
import br.com.fclug.financialaid.server.UserCreation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.register_activity.*
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

class RegisterActivity : AppCompatActivity() {

    private val TAG = RegisterActivity::class.java.simpleName

    var disposable: Disposable? = null

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val username = register_username.text.toString().trim { it <= ' ' }
            if (username.isNotEmpty()) {
                try {
                    val callback: RequestCallback = object : RequestCallback {
                        @Throws(JSONException::class)
                        override fun onSuccess(response: JSONObject) {
                            val exists = response.getJSONArray("result").getJSONObject(0)
                                    .getInt("existing") == 1
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
                        }

                        override fun onFailure(code: Int) {}
                    }
                    val args = JSONObject()
                    args.put("username", username)
                    Log.d(TAG, args.toString())
                    ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_CHECK_USERNAME, args, callback).execute()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                register_feedback.visibility = View.GONE
            }

            val name = register_name.text.toString().trim { it <= ' ' }
            val password = register_password.text.toString().trim { it <= ' ' }
            val passwordRepeat = register_password_repeat.text.toString().trim { it <= ' ' }

            register_button.isEnabled = name.isNotEmpty() && username.isNotEmpty() &&
                    password.isNotEmpty() && passwordRepeat.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        register_name.addTextChangedListener(textWatcher)
        register_username.addTextChangedListener(textWatcher)
        register_password.addTextChangedListener(textWatcher)
        register_password_repeat.addTextChangedListener(textWatcher)

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Registering")
        progressDialog.setMessage("Communicating with server...")

        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(resources.getString(R.string.registration_failed))
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK") { _, _ ->
            register_username.text.clear()
            register_password.text.clear()
            register_password_repeat.text.clear()
        }
        register_button.setOnClickListener {
            val username = register_username.text.toString()
            val password = register_password.text.toString()
            val passwordRepeat = register_password_repeat.text.toString()

            // TODO: validate it on watcher
            if (password == passwordRepeat) {
                progressDialog.show()
                val userCreation = UserCreation(username, register_name.text.toString(), password)
                val api = ServerApi()
                disposable = api.register(userCreation)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({response ->
                            progressDialog.dismiss()
                            val errorList = response.errors

                            if(errorList.isNotEmpty()) {
                                alertDialog.setMessage(errorList.get(0).description)
                                alertDialog.show()
                            } else {
                                Log.d(TAG, response.result.toString())
                                SessionManager.checkLogin(this@RegisterActivity)
                            }
                        }, {error ->
                            progressDialog.dismiss()
                            Toast.makeText(this@RegisterActivity, R.string.api_request_failed,
                                    Toast.LENGTH_SHORT).show()
                            Log.d(TAG, error.message)
                            error.printStackTrace()
                        })
            } else {
                alertDialog.setMessage(resources.getString(R.string.password_not_match))
                alertDialog.show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }
}