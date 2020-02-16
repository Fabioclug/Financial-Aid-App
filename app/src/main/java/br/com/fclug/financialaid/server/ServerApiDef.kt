package br.com.fclug.financialaid.server

import br.com.fclug.financialaid.models.OnlineUser
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ServerApiDef {

    @POST(ServerUtils.ROUTE_CHECK_USERNAME)
    fun checkUsername(@Body username: UsernameCheck): Observable<ApiResponse<ExistingUser>>

    @POST(ServerUtils.ROUTE_REGISTER_USER)
    fun register(@Body user: UserCreation): Observable<ApiResponse<OnlineUser>>

    @POST(ServerUtils.ROUTE_LOGIN)
    fun login(@Body userLogin: UserLogin): Observable<ApiResponse<LoginSession>>
}