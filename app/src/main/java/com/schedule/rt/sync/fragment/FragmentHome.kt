package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.databinding.FragmentHomeBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentHome : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val viewModelUser : ViewModelUser by lazy {
        ViewModelProvider(requireActivity())[ViewModelUser::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = TransitionUtil.enterTransition()
        returnTransition = TransitionUtil.returnTransition()
        exitTransition = TransitionUtil.exitTransition()
        reenterTransition = TransitionUtil.reenterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        actionbar()

        binding.btnLecturer.setOnClickListener {
            DialogUtil.showBottomSheetSchedule(
                requireActivity(),
                "Add Schedule",
                R.drawable.add,
                "Add",
                { bottomSheetBinding, bottomSheet ->

                    bottomSheetBinding.btnYes.setOnClickListener {
                        Toast.makeText(requireContext(), "Add Lecturer", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

    }

    private fun actionbar() {
        binding.toolBar.setNavigationOnClickListener {
            (requireActivity() as ActivityMain).btnDrawer()
        }
    }
}