package ru.androidschool.intensiv.data

import android.os.Parcel
import android.os.Parcelable

class Goods(
    var title: String? = "",
    var voteAverage: Double = 0.0
) {
    val rating: Float
        get() = voteAverage.div(2).toFloat()

}
