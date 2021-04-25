package ru.androidschool.intensiv.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.MenuInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.feed_fragment.*
import kotlinx.android.synthetic.main.feed_header.*
import kotlinx.android.synthetic.main.search_toolbar.view.*
import ru.androidschool.intensiv.R
import ru.androidschool.intensiv.data.MockRepository
import ru.androidschool.intensiv.data.Goods
import ru.androidschool.intensiv.ui.afterTextChanged
import timber.log.Timber

class FeedFragment : Fragment() {

    private val adapter by lazy {
        GroupAdapter<GroupieViewHolder>()
    }

    private lateinit var viewModel: FeedViewModel

    private val options = navOptions {
        anim {
            enter = R.anim.slide_in_right
            exit = R.anim.slide_out_left
            popEnter = R.anim.slide_in_left
            popExit = R.anim.slide_out_right
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(FeedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.feed_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Добавляем recyclerView
        movies_recycler_view.layoutManager = LinearLayoutManager(context)
        movies_recycler_view.adapter = adapter.apply { addAll(listOf()) }

        search_toolbar.search_edit_text.afterTextChanged {
            Timber.d(it.toString())
            if (it.toString().length > MIN_LENGTH) {
                openSearch(it.toString())
            }
        }

        // Используя Мок-репозиторий получаем фэйковый список фильмов


        var moviesList:List<Item>? = null
        if (viewModel.isBannerShow) {
            moviesList = listOf(
            CarrotItem(CarrotData(
                R.drawable.ic_coins,
                R.string.banner_question,
                Pair(R.string.how, LinkedResult(R.string.description, Pair(1, 1)) {
                    findNavController().navigate(R.id.profile_fragment)
                }),
                Pair(R.string.form, LinkedResult(R.string.form, Pair(1, 1)) {
                    findNavController().navigate(R.id.profile_fragment)
                }),

                Pair(R.string.how, LinkedResult(R.string.how, Pair(1, 1), {}))
            ), true, {}),


            MainCardContainer(
                R.string.recommended,
                MockRepository.getMovies().map {
                    GoodsItem(it) { movie ->
                        openMovieDetails(
                            movie
                        )
                    }
                }.toList()
            )
            )
            viewModel.isBannerShow = false
        } else {
            moviesList= listOf(
            MainCardContainer(
                R.string.recommended,
                MockRepository.getMovies().map {
                    GoodsItem(it) { movie ->
                        openMovieDetails(
                            movie
                        )
                    }
                }.toList()
            )
            )
        }

        movies_recycler_view.adapter = adapter.apply { addAll(moviesList) }

        // Используя Мок-репозиторий получаем фэйковый список фильмов
        // Чтобы отобразить второй ряд фильмов
        val newMoviesList = listOf(
            MainCardContainer(
                R.string.upcoming,
                MockRepository.getMovies().map {
                    GoodsItem(it) { movie ->
                        openMovieDetails(movie)
                    }
                }.toList()
            )
        )

        adapter.apply { addAll(newMoviesList) }
    }

    private fun openMovieDetails(goods: Goods) {
        val bundle = Bundle()
        bundle.putString(KEY_TITLE, goods.title)
        findNavController().navigate(R.id.movie_details_fragment, bundle, options)
    }

    private fun openSearch(searchText: String) {
        val bundle = Bundle()
        bundle.putString(KEY_SEARCH, searchText)
        findNavController().navigate(R.id.search_dest, bundle, options)
    }

    override fun onStop() {
        super.onStop()
        search_toolbar.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    companion object {
        const val MIN_LENGTH = 3
        const val KEY_TITLE = "title"
        const val KEY_URL = "payment_url"
        const val KEY_SEARCH = "search"
    }
}
