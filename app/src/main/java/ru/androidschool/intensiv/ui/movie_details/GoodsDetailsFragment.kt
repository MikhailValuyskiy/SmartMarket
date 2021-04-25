package ru.androidschool.intensiv.ui.movie_details

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.goods_details_fragment.*
import ru.androidschool.intensiv.R
import ru.androidschool.intensiv.network.*
import ru.androidschool.intensiv.network.WalletOneApi.Companion.LEGAL_ID
import ru.androidschool.intensiv.network.WalletOneApi.Companion.NEW_EXTERNAL_ID5
import ru.androidschool.intensiv.network.WalletOneApi.Companion.NEW_EXTERNAL_ID6
import ru.androidschool.intensiv.network.WalletOneApi.Companion.NEW_EXTERNAL_ID7
import ru.androidschool.intensiv.ui.feed.FeedFragment.Companion.KEY_URL
import ru.androidschool.intensiv.ui.feed.NotificationHelper
import ru.androidschool.intensiv.ui.feed.WebViewActivity
import timber.log.Timber
import java.util.concurrent.TimeUnit


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MyViewModelFactory(context: Context, invoice: InvoiceBody) :
    ViewModelProvider.Factory {
    private val mContext: Context
    private val mInvoice: InvoiceBody
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainActivityViewModel(mContext, mInvoice) as T
    }

    init {
        mContext = context
        mInvoice = invoice
    }
}

class MainActivityViewModel(val context: Context, val invoice: InvoiceBody) : ViewModel() {

    val statusLiveData = MutableLiveData<String>()


    var tokenCurrent: String = ""
        get() {
            return "Bearer " + field
        }
        set(value) {
            field = value
        }

    @SuppressLint("CheckResult")
    fun checkBill() {
        var status: String = ""

        var isCompleted = false

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
            .concatMap { it ->
                WalletOneClient.apiClient(context).createInvoice(
                    tokenCurrent,
                    invoice
                )
            }
            .compose(ApiErrorHandler<InvoiceIdResponse>())


            // Акцептовать инвойс
            .concatMap {
                WalletOneClient.apiClient(context).acceptInvoice(tokenCurrent, it.id)
            }
            .compose(ApiErrorHandler<InvoiceIdResponse>())


            .concatMap {
                WalletOneClient.apiClient(context).checkInvoice(tokenCurrent, it.id)
            }
            .compose(ApiErrorHandler<InvoiceFullResponse>())

            .doOnNext {
                Timber.d("InvoiceFullResponse" + it.id)
            }
            .delay(20, TimeUnit.SECONDS)
            .flatMap { it ->
                if (it.states != "FINISHED") {
                    Observable.error<Throwable>(Throwable(""))
                } else {
                    Observable.just(it)
                }
            }
            .retry(30)
//            .flatMap { it ->
//                status = it.states
//                Observable.just(it)
//            }
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { statusLiveData.value = status }
//            .repeatUntil { status != "FINISHED" }
//           // .filter { it.states == "FINISHED"  }
//           // .firstElement()
            .subscribe({
                if (it is InvoiceFullResponse) {
                    statusLiveData.value = it.states

//                if (!isCompleted && it.states == "FINISHED") {
//                    isCompleted = true
//                    showNotification(it.getSmzBillUrl() ?: "")
//                }

                    if (it.states == "FINISHED") {
                        showNotification(it.getSmzBillUrl() ?: "")
                    }

                }
            },
                {
                    Timber.d("Error" + it.message)

                })
    }

    fun showNotification(billUrl: String) {

        val args = Bundle()
        args.putString(KEY_URL, billUrl);

        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtras(args)

        NotificationHelper.createSampleDataNotification(
            context, "Товар оплачен!", "Чек внутри", "", true,
            intent
        )
    }


}

class GoodsDetailsFragment : Fragment() {

    lateinit var mainActivityViewModel: MainActivityViewModel

    private var param1: String? = null
    private var param2: String? = null

    var repeatCount = 1

    var tokenCurrent: String = ""
        get() {
            return "Bearer " + field
        }
        set(value) {
            field = value
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.goods_details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val invoiceBody = InvoiceBody(
            NEW_EXTERNAL_ID7,
            fromPerson = FromPerson(getKey("PersonId") ?: "123456"),
            toPerson = ToPerson(LEGAL_ID),
            payload = PayLoad(
                transferNote = TransferNote(note = "Крафтовая коробка"),
                finance = Finance(100),
                providerIn = "TESTCARD",
                providerOut = "TESTCARD",
                params = listOf()
            )
        )

        val viewModelFactory = MyViewModelFactory(requireContext(), invoiceBody)
        mainActivityViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory)
            .get(MainActivityViewModel::class.java)

        val stateObserver = Observer<String> { newState ->
            updateStatus(newState)
        }

        mainActivityViewModel.statusLiveData.observe(requireActivity(), stateObserver)

//        "c1f37217-12b9-4a6e-238f-acd06025d2fd",
        Picasso.get()
            .load("https://cdn.uumarket.ru/z/st/40702a/c1552e/80e504/NTkxMjIyNTIaNDIyMzAxOGh0dHBzOi8vc2FudGEtYXJ0LnJ1L3BpY3R1cmVzL3Byb2R1Y3QvYmlnLzk4MDM3ODE3X2JpZy-qcGca.jpg")
            .into(image_preview)

        var s: String = ""

        buy_button.setOnClickListener {


            saveKey("Clicked", "true")

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
                    saveKey("Token", it.access_token)
                }

                // Создать инвойс
                .flatMap { it ->
                    WalletOneClient.apiClient(requireContext()).createInvoice(
                        tokenCurrent,
                        invoiceBody
                    )
                }
                .compose(ApiErrorHandler<InvoiceIdResponse>())


                // Акцептовать инвойс
                .flatMap {
                    WalletOneClient.apiClient(requireContext()).acceptInvoice(tokenCurrent, it.id)
                }
                .compose(ApiErrorHandler<InvoiceIdResponse>())


                .flatMap {
                    WalletOneClient.apiClient(requireContext()).checkInvoice(tokenCurrent, it.id)
                }
                .compose(ApiErrorHandler<InvoiceFullResponse>())

                .doOnNext {
                    Timber.d("InvoiceFullResponse" + it.id)
                }
                .delay(5, TimeUnit.SECONDS)
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap {
                    getTime(it.states)
                    Observable.just(it)
                }
                .repeat(repeatCount.toLong())
                .doOnNext { updateStatus(it.states) }
                .filter { it.payload.params.isNotEmpty() }
                .firstElement()
                .subscribe({

                    if (it.states == "FINISHED") {
                        mainActivityViewModel.showNotification(it.getSmzBillUrl() ?: "")
                    }

                    updateStatus(it.states)

                    if (it.states == "PROCESSING") {
                        Snackbar.make(
                            view,
                            "Успех!",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        openWebView(it)
                    }

                },
                    {
                        if (it is WalletOneError) {
                            Timber.d("Error" + it.message)

                            Snackbar.make(
                                view,
                                it.message,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    })
        }
    }

    fun getTime(state: String) {
        if (state == "PROCESSING") {
            repeatCount = 5
        }
    }

    fun updateStatus(states: String) {
        when (states) {
            "INIT" -> {
                buy_button.text = "Оформляется"
                buy_button.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.favorite
                    )
                )
            }

            "FINISHED" -> {
                buy_button.text = "Оплачено"
                buy_button.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.blue
                    )
                )
            }
            "CREATED" -> {
                buy_button.text = "Заказ оформлен"
                buy_button.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellow
                    )
                )
            }
            "PROCESSING" -> {
                buy_button.text = "Обрабатывается"
                buy_button.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.selectedControlIndicator
                    )
                )
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (getKey("Clicked") == "true") {
            saveKey("Clicked", "false")
            mainActivityViewModel.checkBill()
        }
    }


    private fun openWebView(r: InvoiceFullResponse) {
//        val bundle = Bundle()
//        bundle.putString(FeedFragment.KEY_URL, r.paymentUrl())
//        findNavController().navigate(R.id.webViewFragment, bundle)

        val browserIntent = Intent(Intent.ACTION_VIEW, r.paymentUrl().toUri())
        ContextCompat.startActivity(requireContext(), browserIntent, null)
    }

    fun saveKey(key: String, value: String) {
        val sharedPref = activity?.getSharedPreferences(
            "my_preffs", Context.MODE_PRIVATE
        )
        with(sharedPref!!.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun getKey(key: String): String? {
        val sharedPref = activity?.getSharedPreferences(
            "my_preffs", Context.MODE_PRIVATE
        )

        return sharedPref?.getString(key, "empty")
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GoodsDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

    }
}
