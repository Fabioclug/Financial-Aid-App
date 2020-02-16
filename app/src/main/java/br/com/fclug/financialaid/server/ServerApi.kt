package br.com.fclug.financialaid.server

import br.com.fclug.financialaid.models.OnlineUser
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ServerApi {
    val retrofit: Retrofit
    val api: ServerApiDef

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)

        val gson = GsonBuilder()
                .setLenient()
                .create()

        retrofit = Retrofit.Builder()
                .baseUrl(ServerUtils.SERVER_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build()

        api = retrofit.create(ServerApiDef::class.java)
    }

    inline fun <reified T:Any> convertErrorResponseBody(response: ResponseBody): T? {
        val converter: Converter<ResponseBody, T> = retrofit.responseBodyConverter(T::class.java, arrayOf())
        return converter.convert(response)
    }

    fun checkUsername(username: String) : Observable<ApiResponse<ExistingUser>> {
        return api.checkUsername(UsernameCheck(username))
    }

    fun register(user: UserCreation) : Observable<ApiResponse<OnlineUser>> {
        return api.register(user)
    }

    fun login(userLogin: UserLogin) : Observable<ApiResponse<LoginSession>> {
        return api.login(userLogin)
    }
}