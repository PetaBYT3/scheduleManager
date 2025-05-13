package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.FragmentNikBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentNik(
    private val fragmentContainer: Int,
    private val toastContainer: Int,
) : Fragment() {

    private var _binding: FragmentNikBinding? = null
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
        _binding = FragmentNikBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        binding.etNik.keyListener = DigitsKeyListener.getInstance("0123456789.")

        binding.btnInputNik.setOnClickListener {
            inputNik()
        }
    }

    private fun inputNik() {
        val etNik = binding.etNik.text.toString()

        val (result, uidLecturer) = vmUser.getLecturerByNik(etNik)

        uidLecturer.observe(viewLifecycleOwner) {
            vmLecturer.sendLecturerUid(it)
        }

        result.observe(viewLifecycleOwner) {
            when (it) {
                "Success" -> {
                    replaceFragmentWithBackStack(
                        fragmentContainer,
                        FragmentLecturerConfirmation(fragmentContainer, toastContainer),
                        "setUp"
                    )
                    binding.pbYes.visibility = View.GONE
                }
                "Empty" -> {
                    replaceToastFragment(toastContainer, FragmentToast(R.drawable.fail, "NIK Cannot Be Empty"))
                    binding.pbYes.visibility = View.GONE
                }
                "Not Exist" -> {
                    replaceToastFragment(toastContainer, FragmentToast(R.drawable.fail, "NIK Not Exist"))
                    binding.pbYes.visibility = View.GONE
                }
                "Error" -> {
                    replaceToastFragment(toastContainer, FragmentToast(R.drawable.fail, "Something Went Wrong"))
                    binding.pbYes.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}