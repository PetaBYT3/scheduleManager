package com.schedule.rt.sync.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.activityViewModels
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.databinding.FragmentLecturerConfirmationBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentLecturerConfirmation(
    private val fragmentContainer: Int,
    private val toastContainer: Int,
) : Fragment() {

    private var _binding: FragmentLecturerConfirmationBinding? = null
    private val binding get() = _binding!!

    private val vmUser: ViewModelUser by activityViewModels()
    private val vmLecturer: ViewModelLecturer by activityViewModels()

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
        _binding = FragmentLecturerConfirmationBinding.inflate(inflater, container, false)
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

        vmLecturer.uidLecturer.observe(viewLifecycleOwner) {
            vmLecturer.getLecturerByUid(it).observe(viewLifecycleOwner) {
                binding.tvTitle.text = it?.nameLecturer
                binding.tvNik.text = it?.nikLecturer
            }
        }

        binding.btnFinish.setOnClickListener {
            pairLecturerUidToUser()
        }
    }

    private fun pairLecturerUidToUser() {
        binding.pbYes.visibility = View.VISIBLE
        vmLecturer.uidLecturer.observe(viewLifecycleOwner) {
            vmUser.addLecturerData(it).observe(viewLifecycleOwner) {
                when (it) {
                    "Success" -> {
                        if (fragmentContainer == R.id.startFragmentContainer) {
                            startActivity(Intent(requireActivity(), ActivityMain::class.java))
                            requireActivity().finish()
                            binding.pbYes.visibility = View.GONE
                        } else {
                            requireActivity().supportFragmentManager.popBackStack("setUp", POP_BACK_STACK_INCLUSIVE)
                        }
                    }
                    "Fail" -> {
                        replaceToastFragment(R.id.startToastContainer, FragmentToast(R.drawable.fail, "Something Went Wrong"))
                        binding.pbYes.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}