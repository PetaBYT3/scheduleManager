package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.schedule.rt.sync.databinding.FragmentSelectRoleBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentSelectRole(
    private val fragmentContainer: Int,
    private val toastContainer: Int,
) : Fragment() {

    private var _binding: FragmentSelectRoleBinding? = null
    private val binding get() = _binding!!

    private val vmUser: ViewModelUser by activityViewModels()

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
        _binding = FragmentSelectRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnStudent.setOnClickListener {
            replaceFragmentWithBackStack(
                fragmentContainer,
                FragmentMajor(fragmentContainer, toastContainer),
                null
            )
        }

        binding.btnLecturer.setOnClickListener {
            replaceFragmentWithBackStack(
                fragmentContainer,
                FragmentNik(fragmentContainer, toastContainer),
                "setUp"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}