package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.databinding.FragmentAdministratorBinding
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator

class FragmentAdministrator : Fragment() {

    private lateinit var binding: FragmentAdministratorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = TransitionUtil.enterTransition()
        returnTransition = TransitionUtil.returnTransition()
        exitTransition = TransitionUtil.exitTransition()
        reenterTransition = TransitionUtil.reenterTransition()
    }

    private val viewModelAdministrator: ViewModelAdministrator by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAdministratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        binding.toolBar.setNavigationOnClickListener {
            (requireActivity() as ActivityMain).btnDrawer()
        }

        binding.btnLecturer.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragmentContainer, FragmentDataLecturer::class.java, null)
                addToBackStack(null)
            }
        }

        binding.btnMajor.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragmentContainer, FragmentDataMajor::class.java, null)
                addToBackStack(null)
            }
        }

        binding.btnBuilding.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragmentContainer, FragmentDataBuilding::class.java, null)
                addToBackStack(null)
            }
        }
    }
}