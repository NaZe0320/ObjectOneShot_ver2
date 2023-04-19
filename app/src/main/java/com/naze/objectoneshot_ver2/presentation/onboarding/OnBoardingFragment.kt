package com.naze.objectoneshot_ver2.presentation.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.naze.objectoneshot_ver2.R
import com.naze.objectoneshot_ver2.databinding.FragmentViewpagerBinding
import com.naze.objectoneshot_ver2.util.BindingFragment
import com.naze.objectoneshot_ver2.util.ViewPagerAdapter

class OnBoardingFragment: BindingFragment<FragmentViewpagerBinding>(R.layout.fragment_viewpager) {

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager2
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPagerAdapter = ViewPagerAdapter(this, getTipsList())
        viewPager = binding.viewPager
        viewPager.adapter = viewPagerAdapter
        binding.springDotsIndicator.attachTo(viewPager)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == viewPagerAdapter.itemCount - 1) {
                    binding.btnSkip.visibility = View.GONE
                    binding.btnStart.visibility = View.VISIBLE
                } else {
                    binding.btnSkip.visibility = View.VISIBLE
                    binding.btnStart.visibility = View.GONE
                }
            }
        })

        binding.btnSkip.setOnClickListener {
            parentFragmentManager.popBackStackImmediate()
        }

        binding.btnStart.setOnClickListener {
            parentFragmentManager.popBackStackImmediate()
        }
    }

    private fun getTipsList(): ArrayList<Fragment> {
        return arrayListOf()
    }
}