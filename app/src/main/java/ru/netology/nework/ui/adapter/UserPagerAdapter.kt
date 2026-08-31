package ru.netology.nework.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.netology.nework.ui.fragments.userfragment.UserJobsFragment
import ru.netology.nework.ui.fragments.userfragment.UserWallFragment

class UserPagerAdapter(
    fragment: Fragment,
    private val userId: Int?
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserWallFragment.newInstance(userId)
            1 -> UserJobsFragment.newInstance(userId)
            else -> throw IllegalArgumentException("Неверная позиция: $position")
        }
    }
}