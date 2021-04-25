package ru.androidschool.intensiv.ui.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import ru.androidschool.intensiv.R

private const val ARG_PARAM1 = "imageId"
private const val ARG_PARAM2 = "title"
private const val ARG_PARAM3 = "subtitle"

class SplashTwoFragment : androidx.fragment.app.Fragment() {

    private var param1: Int? = null
    private var title: String? = null
    private var subTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
            title = it.getString(ARG_PARAM2)
            subTitle = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_splash_two, container, false)

        val mainImage = view.findViewById<ImageView>(R.id.main_image)
        val title = view.findViewById<TextView>(R.id.title)
        val subtitle = view.findViewById<TextView>(R.id.subtitle)


        mainImage.setImageResource(param1 ?: R.drawable.ic_track_expenses)
        title.text = this.title
        subtitle.text = this.subTitle

        return view
    }

    companion object {

        @JvmStatic
        fun newInstance(@DrawableRes imageId: Int, title: String, subtitle: String) =
            SplashTwoFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, imageId)
                    putString(ARG_PARAM2, title)
                    putString(ARG_PARAM3, subtitle)
                }
            }
    }
}
