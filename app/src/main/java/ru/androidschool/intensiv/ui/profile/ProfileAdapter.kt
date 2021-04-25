package ru.androidschool.intensiv.ui.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.androidschool.intensiv.ui.watchlist.*

class ProfileAdapter(fragment: Fragment, private val itemsCount: Int) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return itemsCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                OnboardingFragment.newInstance()
            }
            1 -> {
                MyCardFragment.newInstance("", "")
            }
            2 -> {
                StatisticFragment.newInstance("","")
            }
            3 -> {
                TransactionFragment.newInstance("","")
            }
            4 -> {
                AnalyticsFragment.newInstance("","")
            }
            5 -> {
                PortfolioDividendDiagramFragment.newInstance(1)
            }
            else -> WatchlistFragment.newInstance()
        }
    }
}
