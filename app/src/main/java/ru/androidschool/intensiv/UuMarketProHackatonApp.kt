package ru.androidschool.intensiv

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import ru.androidschool.intensiv.ui.feed.NotificationHelper
import timber.log.Timber

class UuMarketProHackatonApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        initDebugTools()

        NotificationHelper.createNotificationChannel(
            this,
            NotificationManagerCompat.IMPORTANCE_DEFAULT, false,
            getString(R.string.app_name), "Уумаркет."
        )
    }
    private fun initDebugTools() {
        if (BuildConfig.DEBUG) {
            initTimber()
        }
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

    companion object {
        var instance: UuMarketProHackatonApp? = null
            private set
    }
}
