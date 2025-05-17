package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.FragmentSignUpBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentSignUp : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
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
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
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

        binding.btnSignUp.setOnClickListener {
            binding.pbYes.visibility = View.VISIBLE
            val etEmail = binding.etEmail.text.toString()
            val etPassword = binding.etPassword.text.toString()
            val retypePassword = binding.etRetypePassword.text.toString()

            vmUser.signUp(etEmail, etPassword, retypePassword).observe(viewLifecycleOwner) {
                when (it) {
                    "Success" -> {
                        replaceToastFragment(R.id.startToastContainer, FragmentToast(R.drawable.check, "Sign Up Success"))
                        replaceFragmentWithoutBackStack(
                            R.id.startFragmentContainer,
                            FragmentSelectRole(R.id.startFragmentContainer, R.id.startToastContainer)
                        )
                        binding.pbYes.visibility = View.INVISIBLE
                    }
                    "Used Email" -> {
                        replaceToastFragment(
                            R.id.startToastContainer,
                            FragmentToast(R.drawable.fail, "Email Already Used")
                        )
                        binding.pbYes.visibility = View.INVISIBLE
                    }
                    "Invalid Email" -> {
                        replaceToastFragment(
                            R.id.startToastContainer,
                            FragmentToast(R.drawable.fail, "Please Enter Valid Email")
                        )
                        binding.pbYes.visibility = View.INVISIBLE
                    }
                    "Password Short" -> {
                        replaceToastFragment(
                            R.id.startToastContainer,
                            FragmentToast(R.drawable.fail, "Password Too Short, Must Be 6 Character At Least")
                        )
                        binding.pbYes.visibility = View.INVISIBLE
                    }
                    "Empty" -> {
                        replaceToastFragment(
                            R.id.startToastContainer,
                            FragmentToast(R.drawable.fail, "Fill All Fields")
                        )
                        binding.pbYes.visibility = View.INVISIBLE
                    }
                    "Password Not Match" -> {
                        replaceToastFragment(
                            R.id.startToastContainer,
                            FragmentToast(R.drawable.fail, "Password Not Match")
                        )
                        binding.pbYes.visibility = View.INVISIBLE
                    }
                    else -> {
                        replaceToastFragment(
                            R.id.startToastContainer,
                            FragmentToast(R.drawable.fail, "Something Went Wrong")
                        )
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