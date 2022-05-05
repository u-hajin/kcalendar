package com.kkwakjavacoding.kcalendar.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kkwakjavacoding.kcalendar.fragment.GraphFragment
import com.kkwakjavacoding.kcalendar.fragment.KcalendarFragment

class MyViewPageAdapter(fragmentActivity: FragmentActivity)
    :FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> KcalendarFragment()
            1-> GraphFragment()
            else -> KcalendarFragment()
        }
    }

}