package com.schedule.rt.sync.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.databinding.FragmentSignInBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentSignIn : Fragment() {

    private var _binding: FragmentSignInBinding? = null
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
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
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

        binding.btnSignIn.setOnClickListener {
            binding.pbYes.visibility = View.VISIBLE
            val etEmail = binding.etEmail.text.toString()
            val etPassword = binding.etPassword.text.toString()

            vmUser.signIn(etEmail, etPassword).observe(viewLifecycleOwner) {
                when (it) {
                    "Success" -> {
                        vmUser.getUser().observe(viewLifecycleOwner) {
                            val uidMajor = it?.uidMajor
                            val uidLevel = it?.uidLevel
                            val uidClasses = it?.uidClasses
                            val uidLecturer = it?.uidLecturer
                            if (uidMajor != null && uidLevel != null && uidClasses != null || uidLecturer != null) {
                                startActivity(Intent(requireActivity(), ActivityMain::class.java))
                                requireActivity().finish()
                            } else {
                                binding.pbYes.visibility = View.INVISIBLE
                                replaceToastFragment(R.id.startToastContainer, FragmentToast(R.drawable.check, "Sign In Success"))
                                replaceFragmentWithoutBackStack(
                                    R.id.startFragmentContainer,
                                    FragmentSelectRole(R.id.startFragmentContainer, R.id.startToastContainer)
                                )
                            }
                        }
                    }
                    "Empty" -> {
                        replaceToastFragment(R.id.startToastContainer, FragmentToast(R.drawable.fail, "Fill All Fields"))
                        binding.pbYes.visibility = View.INVISIBLE
                    }
                    "Fail" -> {
                        replaceToastFragment(R.id.startToastContainer, FragmentToast(R.drawable.fail, "Email Or Password Incorrect"))
                        binding.pbYes.visibility = View.INVISIBLE
                    }
                    "Error" -> {
                        replaceToastFragment(R.id.startToastContainer, FragmentToast(R.drawable.fail, "Something Went Wrong"))
                        binding.pbYes.visibility = View.INVISIBLE
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