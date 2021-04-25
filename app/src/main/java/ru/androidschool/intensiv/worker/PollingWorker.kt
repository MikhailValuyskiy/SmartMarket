package ru.androidschool.intensiv.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.androidschool.intensiv.network.*
import timber.log.Timber

class PollingWorker(
    private val invoice: InvoiceBody,
    private val context: Context,
    params: WorkerParameters
) :
    Worker(context, params) {

    var tokenCurrent: String = ""
        get() {
            return "Bearer " + field
        }
        set(value) {
            field = value
        }

    @SuppressLint("CheckResult")
    override fun doWork(): Result {

        val map = HashMap<String, String>()
        map.put("username", TokenApiInterface.userName)
        map.put("password", TokenApiInterface.passWord)
        map.put("grant_type", TokenApiInterface.grantType)
        map.put("token_type", TokenApiInterface.tokenType)
        map.put("client_secret", TokenApiInterface.clientSecret)
        map.put("client_id", TokenApiInterface.clientId)


        // Получить токен
        TokenApiClient.apiClient.getToken(map)
            .compose(ApiErrorHandler<TokenResponse>())
            .doOnNext { Timber.d("New 666 Token" + it.access_token) }
            .doOnNext {
                tokenCurrent = it.access_token
            }

            // Создать инвойс
            .flatMap { it ->
                WalletOneClient.apiClient(context).createInvoice(
                    tokenCurrent,
                    invoice
                )
            }
            .compose(ApiErrorHandler<InvoiceIdResponse>())


            // Акцептовать инвойс
            .flatMap {
                WalletOneClient.apiClient(context).acceptInvoice(tokenCurrent, it.id)
            }
            .compose(ApiErrorHandler<InvoiceIdResponse>())


            .flatMap {
                WalletOneClient.apiClient(context).checkInvoice(tokenCurrent, it.id)
            }
            .compose(ApiErrorHandler<InvoiceFullResponse>())

            .doOnNext {
                Timber.d("InvoiceFullResponse" + it.id)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
//                .delay(10, TimeUnit.SECONDS)
//                .repeat(1)
            .subscribe({
                Timber.d("Error" + it.getSmzBillUrl())
            },
                {
                    if (it is WalletOneError) {
                        Timber.d("Error" + it.message)

                    }
                })

        return Result.success()

    }


    companion object {
        private const val SIMPLE_WORKER_TAG = "SimpleWorkerTag"

        // 1. Метод для создания OneTimeWorkRequest
        private fun createWorkRequest(data: Data): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(PollingWorker::class.java)
                .setInputData(data)
                .addTag(SIMPLE_WORKER_TAG)
                .build()
        }

        // 2. Метод для запуска
        fun startWork(context: Context) {
            val work = createWorkRequest(Data.EMPTY)
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    SIMPLE_WORKER_TAG,
                    ExistingWorkPolicy.APPEND,
                    work
                )
        }

        // 3. Метод для остановки
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(SIMPLE_WORKER_TAG)
        }
    }
}