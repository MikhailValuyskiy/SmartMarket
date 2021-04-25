package ru.androidschool.intensiv.network

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface TokenApiInterface {

    @Headers( "Content-Type: application/x-www-form-urlencoded")
    @POST("auth/realms/plutdev/protocol/openid-connect/token")
    @FormUrlEncoded
    fun getToken(
        @FieldMap params:Map<String,String>
    ): Observable<Response<TokenResponse>>


    companion object {
        const val userName = "agent12"
        const val passWord = "agent12"
        const val grantType = "password"
        const val tokenType = "bearer"

        const val clientSecret = "e5d2b5c1-4211-48aa-b272-91d75f4ab0e5"
        const val clientId = "agentService"

        const val legal_id = "2f663d3f-09b8-4359-8b01-bb14251f7b33"

        const val personId =  "4ea9141c-35f4-4524-94c5-1a7d0f4f8b0c"
    }
}

data class TokenResponse(
    val access_token: String,
    val expires_in: Int,
    val refresh_expires_in: Int,
    val refresh_token: String,
    val token_type: String,
    val not_before_policy: Int,
    val session_state: String,
    val scope: String
)
