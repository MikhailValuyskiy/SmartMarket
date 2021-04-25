package ru.androidschool.intensiv.network

import android.content.Context
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object WalletOneClient {

    private const val BASE_URL = "http://185.209.114.26:8080"

    private fun client(context: Context): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ChuckInterceptor(context))
        .addInterceptor(HttpLoggingInterceptor(CustomHttpLogging()).apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    fun apiClient(context: Context): WalletOneApi {

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client(context))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        return retrofit.create(WalletOneApi::class.java)
    }
}