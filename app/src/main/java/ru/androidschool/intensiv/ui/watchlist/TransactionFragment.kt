package ru.androidschool.intensiv.ui.watchlist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_statistic.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import ru.androidschool.intensiv.R
import ru.androidschool.intensiv.ui.feed.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TransactionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        return inflater.inflate(R.layout.fragment_transaction, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transaction_recycler_view.layoutManager = LinearLayoutManager(context)
        transaction_recycler_view.adapter = adapter.apply {
            addAll(
                listOf(
                    TransItem(
                        Preview(
                            "Крафтовая коробка",
                            "Ссылка на чек",
                            "Сумма 100 ₽",
                            "25.04.2021",
                            "https://himself-ktr.nalog.ru/api/v1/receipt/263012809589/2005x2e3x6/print"
                        )
                    ) {
                        open(it.url)
                    },
                    TransItem(
                        Preview(
                            "Крафтовая коробка",
                            "Ссылка на чек",
                            "Сумма 100 ₽",
                            "25.04.2021",
                            "https://himself-ktr.nalog.ru/api/v1/receipt/263012809589/2005x2e3x6/print"
                        )
                    ) {
                        open(it.url)
                    }
                )
            )
        }
    }

    fun open(url:String){
        val args = Bundle()
        args.putString(FeedFragment.KEY_URL, url);

        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtras(args)
        startActivity(intent)
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
            TransactionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}