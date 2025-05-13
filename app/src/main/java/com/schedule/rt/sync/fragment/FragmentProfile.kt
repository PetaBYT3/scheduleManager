package com.schedule.rt.sync.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.activity.ActivityStart
import com.schedule.rt.sync.databinding.FragmentProfileBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.removeFragmentFromContainer
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentProfile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val vmUser: ViewModelUser by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()
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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
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

        binding.btnEditData.setOnClickListener {
            replaceFragmentWithBackStack(R.id.mainFragmentContainer,
                FragmentSelectRole(R.id.mainFragmentContainer, R.id.mainToastContainer), "setUp")
        }

        vmUser.getUser().observe(viewLifecycleOwner) {
            val uidLecturer = it?.uidLecturer

            if (uidLecturer == null) {
                profileStudent()
            } else {
                profileLecturer()
            }
        }

        binding.btnSignOut.setOnClickListener {
            val fragmentMessage = FragmentMessage(
                tittle = "Sign Out",
                message = "Are You Sure You Want To Sign Out?",
                ivYes = R.drawable.exit,
                tvYes = "Sign Out",
                onYesClick = {
                    FirebaseAuth.getInstance().signOut()
                    removeFragmentFromContainer(R.id.mainBottomSheetContainer)
                    startActivity(Intent(requireActivity(), ActivityStart::class.java))
                    requireActivity().finish()
                }
            )
            replaceFragmentWithoutBackStack(R.id.mainBottomSheetContainer, fragmentMessage)
        }
    }

    private fun profileStudent() {
        vmUser.getUser().observe(viewLifecycleOwner) {
            binding.tvTitle.text = it?.nameUser

            val uidMajor = it?.uidMajor
            val uidLevel = it?.uidLevel
            val uidClasses = it?.uidClasses

            vmMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
                binding.tvData1.text = it?.nameMajor
            }

            vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
                binding.tvData2.text = it?.level
            }

            vmClasses.getClassesByUid(uidClasses).observe(viewLifecycleOwner) {
                binding.tvData3.text = it?.nameClasses
            }
        }

        binding.tvData4.text = FirebaseAuth.getInstance().currentUser?.email
    }

    private fun profileLecturer() {
        vmUser.getUser().observe(viewLifecycleOwner) {
            val uidLecturer = it?.uidLecturer

            vmLecturer.getLecturerByUid(uidLecturer).observe(viewLifecycleOwner) {
                binding.tvTitle.text = it?.nameLecturer ?: "-"

                binding.tvData1.text = it?.nikLecturer ?: "-"

                val administratorAccess = it?.administratorAccess.toString()
                if (administratorAccess == "True") {
                    binding.tvData2.text = "Administrator"
                } else {
                    binding.tvData2.text = "Lecturer"
                }

                val uidMajor = it?.uidMajorManager
                vmMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
                    if (it != null) {
                        binding.tvData3.text = buildString {
                            append(it.nameMajor)
                            append(" Manager")
                        }
                    } else {
                        binding.tvData3.text = "Not managing any major"
                    }
                }
            }
        }

        binding.tvData4.text = FirebaseAuth.getInstance().currentUser?.email
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeFragmentFromContainer(R.id.mainBottomSheetContainer)
        _binding = null
    }
}