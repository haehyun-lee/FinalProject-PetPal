package com.petpal.mungmate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.petpal.mungmate.databinding.FragmentMyPageBinding

class MyPageFragment : Fragment() {
    private lateinit var _fragmentMyPageBinding: FragmentMyPageBinding
    private val fragmentMyPageBinding get() = _fragmentMyPageBinding
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _fragmentMyPageBinding = FragmentMyPageBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        fragmentMyPageBinding.run {
            buttonManagePet.setOnClickListener {
                mainActivity.navigate(R.id.action_mainFragment_to_manage_pet)
            }

            buttonWalkHistory.setOnClickListener {
                mainActivity.navigate(R.id.action_mainFragment_to_walk_history)
            }

            buttonGoToMatchHistory.setOnClickListener {
                mainActivity.navigate(R.id.action_mainFragment_to_match_history)
            }

            buttonGoToManageBlock.setOnClickListener {
               mainActivity.navigate(R.id.action_mainFragment_to_manage_block)
            }

            buttonAnnouncement.setOnClickListener {
                mainActivity.navigate(R.id.action_mainFragment_to_announcement)
            }

            buttonFAQ.setOnClickListener {
                mainActivity.navigate(R.id.action_mainFragment_to_FAQFragment)
            }

            buttonInquire.setOnClickListener {
                mainActivity.navigate(R.id.action_mainFragment_to_inquiryFragment)
            }

            buttonOrderHistory.setOnClickListener {
                mainActivity.navigate(R.id.action_mainFragment_to_order_history)
            }
        }
        return fragmentMyPageBinding.root
    }

}