package ru.androidschool.intensiv.ui.watchlist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_statistic.*
import ru.androidschool.intensiv.R
import ru.androidschool.intensiv.ui.feed.Preview
import ru.androidschool.intensiv.ui.feed.PreviewItem
import java.util.*
import java.util.concurrent.TimeUnit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class StatisticFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_statistic, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


            add_button.setOnClickListener {
                if  ( goods_recycler_view.size == 2) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivity(intent)
                }

                Observable.just("")
                    .delay(1,TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        goods_recycler_view.adapter = adapter.apply {
                            addAll(
                                listOf(
                                    PreviewItem(Preview("Игрушка Мики", "Игрушки", "100 ₽", "Осталось 20","", R.drawable.micky), {})
                                )
                            )
                        }
                    }
            }


        goods_recycler_view.layoutManager = LinearLayoutManager(context)
        goods_recycler_view.adapter = adapter.apply {
            addAll(
                listOf(
                    PreviewItem(Preview("Коробка 1", "Открытки", "100 ₽", "Осталось 20",""), {}),
                    PreviewItem(Preview("Игрушка 2", "Игрушки", "200 ₽", "Осталось 2",""), {})
                )
            )
        }
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
            StatisticFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}