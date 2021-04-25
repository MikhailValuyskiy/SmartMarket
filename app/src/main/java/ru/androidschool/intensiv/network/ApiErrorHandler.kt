package ru.androidschool.intensiv.network

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import okhttp3.ResponseBody
import retrofit2.Response
import ru.androidschool.intensiv.network.ApiErrorHandler.Companion.STATUS_NO_NETWORK
import java.net.UnknownHostException

class ApiErrorHandler<T> : ObservableTransformer<Response<T>, T> {

    override fun apply(upstream: Observable<Response<T>>): ObservableSource<T> {
        return upstream
            .onErrorResumeNext { error: Throwable ->
                val throwable = when (error) {
                    is UnknownHostException -> Error.NoNetwork()
                    else -> error
                }
                return@onErrorResumeNext Observable.error(throwable)
            }
            .map { response ->
                return@map if (response.isSuccessful && (response.body() != null)) {
                    response.body()
                } else {
                    throw ServerErrorResultFactory.create(response)
                }
            }
    }

    companion object {

        const val STATUS_NO_NETWORK = -1
    }
}

interface IErrorFactory {

    fun <InputResponseType> create(response: Response<InputResponseType>): WalletOneError

    fun parseMeta(errorBody: ResponseBody?): WalletOneError? {
        if (errorBody == null) return null
        val content = errorBody.string()
        if (content.isNullOrEmpty()) return null
        return try {
             Gson().fromJson(content, WalletOneError::class.java)
        } catch (e: Exception) {
            null
        }
    }
}


object ServerErrorResultFactory : IErrorFactory {
    override fun <InputResponseType> create(response: Response<InputResponseType>): WalletOneError {
        val meta = parseMeta(response.errorBody())
        return meta!!
    }
}


sealed class Error(
    val status: Int,
    val code: String,
    val codeValue: String,
    override val message: String?
) : RuntimeException(message) {


    class NoNetwork : Error(STATUS_NO_NETWORK, "", "", "Network unavailable")
}



