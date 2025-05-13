package com.schedule.rt.sync.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.databinding.FragmentSplashBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentSplash : Fragment() {

    private var _binding: FragmentSplashBinding? = null
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
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                replaceFragmentWithoutBackStack(R.id.startFragmentContainer, FragmentStart())
            } else {
                vmUser.getUser().observe(viewLifecycleOwner) {
                    val uidMajor = it?.uidMajor
                    val uidLevel = it?.uidLevel
                    val uidClasses = it?.uidClasses
                    val uidLecturer = it?.uidLecturer

                    val isLecturerValid = uidLecturer != null
                    val isStudentValid = uidMajor != null && uidLevel != null && uidClasses != null

                    if (isLecturerValid || isStudentValid) {
                        startActivity(Intent(requireContext(), ActivityMain::class.java))
                        requireActivity().finish()
                    } else {
                        replaceFragmentWithoutBackStack(R.id.startFragmentContainer, FragmentSelectRole(R.id.startFragmentContainer, R.id.startToastContainer))
                    }
                }
            }
        }, 500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}