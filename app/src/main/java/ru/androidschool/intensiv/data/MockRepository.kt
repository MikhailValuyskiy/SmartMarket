package ru.androidschool.intensiv.data

object MockRepository {

    fun getMovies(): List<Goods> {

        val moviesList = mutableListOf<Goods>()
        for (x in 0..10) {
            val movie = Goods(
                title = "Handmade $x",
                voteAverage = 10.0 - x
            )
            moviesList.add(movie)
        }

        return moviesList
    }
}
