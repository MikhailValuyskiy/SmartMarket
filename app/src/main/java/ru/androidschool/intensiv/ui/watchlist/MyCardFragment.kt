package ru.androidschool.intensiv.ui.watchlist

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my_card.*
import ru.androidschool.intensiv.R
import ru.androidschool.intensiv.network.*
import ru.androidschool.intensiv.network.WalletOneApi.Companion.EXTERNAL_ID
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MyCardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    var tokenCurrent: String = ""
        get() {
            return "Bearer " + field
        }
        set(value) {
            field = value
        }


    private val adapter by lazy {
        GroupAdapter<GroupieViewHolder>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_card, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (getKey("CardNumber") != "empty") {
            card_editor.visibility = View.GONE
            cards_recycler_view.visibility = View.VISIBLE
            setCard()
        } else {
            card_editor.visibility = View.VISIBLE
            cards_recycler_view.visibility = View.GONE
        }

        val map = HashMap<String, String>()
        map.put("username", TokenApiInterface.userName)
        map.put("password", TokenApiInterface.passWord)
        map.put("grant_type", TokenApiInterface.grantType)
        map.put("token_type", TokenApiInterface.tokenType)
        map.put("client_secret", TokenApiInterface.clientSecret)
        map.put("client_id", TokenApiInterface.clientId)

        card_number.setText("4929509947106878")
        send_button.setOnClickListener {
            val number = card_number.text.toString()

            TokenApiClient.apiClient.getToken(map)
                .compose(ApiErrorHandler<TokenResponse>())
                .doOnNext { Timber.d("New 666 Token" + it.access_token) }
                .doOnNext { saveKey("Token", it.access_token) }
                .flatMap { it ->
                    WalletOneClient.apiClient(requireContext()).bindCard(
                        "Bearer " + it.access_token, CardBody(
                            getKey("PersonId") ?: "", number
                        )
                    )
                }
                .compose(ApiErrorHandler<CardResponse>())
                .doOnNext { it -> saveKey("CardNumber", it.number) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    card_editor.visibility = View.GONE
                    cards_recycler_view.visibility = View.VISIBLE

                    Snackbar.make(
                        view,
                        "Карта успешно привязана!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Timber.d("CardNumber" + getKey("CardNumber"))
                    setCard()

                }, {
                    Snackbar.make(
                        view,
                        it.toString(),
                        Snackbar.LENGTH_SHORT
                    ).show()
                })
        }

        TokenApiClient.apiClient.getToken(map)
            .compose(ApiErrorHandler<TokenResponse>())
            .doOnNext { Timber.d("New 666 Token" + it.access_token) }
            .doOnNext {
                tokenCurrent = it.access_token
                saveKey("Token", it.access_token)
            }
            .flatMap { it ->
                WalletOneClient.apiClient(requireContext()).bindPerson(
                    tokenCurrent,
                    getKey("PersonId") ?: ""
                )
            }
            .compose(ApiErrorHandler<BindPersonResponse>())
            .doOnNext {
                Timber.d("BindPerson" + it.id)
            }

            .concatMap { it ->
                WalletOneClient.apiClient(requireContext()).checkBind(
                    tokenCurrent,
                    getKey("PersonId") ?: "",
                    it.id,
                    ExternalIdBody(EXTERNAL_ID)
                )
            }
            .compose(ApiErrorHandler<CheckBindResponse>())

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                Timber.d("PersonId" + getKey("PersonId"))
                status_value.setText(it.payload.result)

                Snackbar.make(
                    view,
                    "Вы успешно привязаны к системе!",
                    Snackbar.LENGTH_SHORT
                ).show()

                Timber.d("New Key" + getKey("Token"))

            },
                {
                    Timber.d("Error" + it.message)
                    if (it is WalletOneError) {
                        Timber.d("Error" + it.message)

                        Snackbar.make(
                            view,
                            it.message,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                })

        TokenApiClient.apiClient.getToken(map)
            .compose(ApiErrorHandler<TokenResponse>())
            .doOnNext { Timber.d("New Token" + it.access_token) }
            .doOnNext {
                tokenCurrent = it.access_token
                saveKey("Token", it.access_token)
            }
            .flatMap {
                tokenCurrent = it.access_token
                WalletOneClient.apiClient(requireContext()).getIncome(
                    tokenCurrent, requestBody = WalletOneApi.IncomeRequestBody(
                        from = "2021-01-01T12:08:56.235-07:00",
                        inn = "263012809589",
                        limit = 100,
                        offset = 0,
                        to = "2021-01-01T12:08:56.235-07:00"
                    )
                )
            }
            .compose(ApiErrorHandler<IncomeResponse>())

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("Log" + it)
            }, {
                Timber.d(it.message)
            }
            )

    }

    fun setCard() {
        val cardNumber = getKey("CardNumber")

        // Добавляем recyclerView
        cards_recycler_view.layoutManager = LinearLayoutManager(context)
        cards_recycler_view.adapter = adapter.apply {
            addAll(
                listOf(
                    MyCardItem(
                        cardNumber ?: "",
                        requireContext(),
                        Balance(100, 0, CurrencyUtils.RUB)
                    ) {})
            )
        }
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyCardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyCardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}