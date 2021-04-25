package ru.androidschool.intensiv.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.androidschool.intensiv.R
import ru.androidschool.intensiv.ui.watchlist.RegistrationFragment

class RegistrationActivity : AppCompatActivity() {

    private var title: String? = null
    private var iconId: Int? = null
    private var subId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        title = intent.getStringExtra("title")
        iconId = intent.getIntExtra("icon_id", 0)
        subId = intent.getLongExtra("sub_id", 0)


        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.title = "Регистрация"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        showFragment(RegistrationFragment.newInstance(title.orEmpty(), ""))
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_container, fragment, fragment.javaClass.name)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}