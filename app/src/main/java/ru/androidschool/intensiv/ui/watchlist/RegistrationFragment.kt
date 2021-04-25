package ru.androidschool.intensiv.ui.watchlist

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_registration.*
import ru.androidschool.intensiv.R
import ru.androidschool.intensiv.network.*
import ru.androidschool.intensiv.network.TokenApiInterface.Companion.clientId
import ru.androidschool.intensiv.network.TokenApiInterface.Companion.clientSecret
import ru.androidschool.intensiv.network.TokenApiInterface.Companion.grantType
import ru.androidschool.intensiv.network.TokenApiInterface.Companion.passWord
import ru.androidschool.intensiv.network.TokenApiInterface.Companion.tokenType
import ru.androidschool.intensiv.network.TokenApiInterface.Companion.userName
import ru.androidschool.intensiv.network.WalletOneApi.Companion.EXTERNAL_ID
import timber.log.Timber

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class RegistrationFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = view.findViewById<EditText>(R.id.name)
        val surName = view.findViewById<EditText>(R.id.surname)
        val patronum = view.findViewById<EditText>(R.id.patronum)
        val inn = view.findViewById<EditText>(R.id.inn)
        val phone = view.findViewById<EditText>(R.id.phone_number)

        val map = HashMap<String, String>()
        map.put("username", userName)
        map.put("password", passWord)
        map.put("grant_type", grantType)
        map.put("token_type", tokenType)
        map.put("client_secret", clientSecret)
        map.put("client_id", clientId)

//        externalId = "c1f37217-12b9-4a6e-938f-acd06025d2fd", - 1
//        externalId = "c1f37217-12b9-4a6e-938f-acd06045d5fd", - 2
        send_button.setOnClickListener {
            val personBody = CreatePersonBody(
                externalId = EXTERNAL_ID,
                nationality = "RUS",
                initials = Initials(
                    firstName = name.text.toString(),
                    lastName = surName.text.toString(),
                    patronymic = patronum.text.toString()
                ),
                inn = Inn(inn.text.toString()),
                phone = Phone(phone = phone.text.toString())
            )


            // Создание PersonId
            // Получение токена
            TokenApiClient.apiClient.getToken(map)
                .compose(ApiErrorHandler<TokenResponse>())
                .doOnNext { Timber.d("New 666 Token" + it.access_token) }
                .doOnNext {
                    saveKey("Token", it.access_token)
                    tokenCurrent = it.access_token
                }

                // Создание PersonId
                .flatMap { token ->
                    WalletOneClient.apiClient(requireContext()).createPerson(
                        "Bearer " + token.access_token,
                        personBody
                    )
                }
                .compose(ApiErrorHandler<CreatePersonResponse>())
                .doOnNext {
                    saveKey("PersonId", it.id)
                    Timber.d("PersonId" + getKey("PersonId"))
                }

                // Привязка Person к системе
                .flatMap { WalletOneClient.apiClient(requireContext()).bindPerson(tokenCurrent, it.id) }
                .compose(ApiErrorHandler<BindPersonResponse>())
                .doOnNext {
                    Timber.d("BindPerson" + it.id)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    saveKey("PersonId", it.id)
                    Timber.d("PersonId" + getKey("PersonId"))

                    Snackbar.make(
                        view,
                        "Вы успешно зарегистрировались!" + it.id,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    activity?.finish()

                    Timber.d("New Key" + getKey("Token"))

                },
                    {

                        Snackbar.make(
                            view,
                            "Вы успешно зарегистрировались!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        activity?.finish()

//                        if (it is WalletOneError) {
//                            Timber.d("Error" + it.message)
//
//                            Snackbar.make(
//                                view,
//                                it.message,
//                                Snackbar.LENGTH_SHORT
//                            ).show()
//                        }


                    })
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

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegistrationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}