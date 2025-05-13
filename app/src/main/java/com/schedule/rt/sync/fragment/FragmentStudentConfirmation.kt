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
import com.schedule.rt.sync.databinding.FragmentStudentConfirmationBinding
import com.schedule.rt.sync.dataclass.DataClassUser
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithoutBackStack
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceToastFragment
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelData
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelUser

class FragmentStudentConfirmation(
    private val fragmentContainer: Int,
    private val toastContainer: Int,
) : Fragment() {

    private var _binding: FragmentStudentConfirmationBinding? = null
    private val binding get() = _binding!!

    private val vmUser: ViewModelUser by activityViewModels()
    private val vmData: ViewModelData by activityViewModels()
    private val vmMajor: ViewModelMajor by activityViewModels()
    private val vmLevel: ViewModelLevel by activityViewModels()
    private val vmClasses: ViewModelClasses by activityViewModels()

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
        _binding = FragmentStudentConfirmationBinding.inflate(inflater, container, false)
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

        val uidMajor = vmData.uidMajor.value
        val uidLevel = vmData.uidLevel.value
        val uidClasses = vmData.uidClasses.value
        val nameUser = vmData.nameUser.value

        binding.tvTitle.text = nameUser ?: "-"

        vmMajor.getMajorByUid(uidMajor).observe(viewLifecycleOwner) {
            binding.tvMajor.text = it?.nameMajor ?: "-"
        }

        vmLevel.getLevelByUid(uidLevel).observe(viewLifecycleOwner) {
            binding.tvLevel.text = "Level ${it?.level}" ?: "-"
        }

        vmClasses.getClassesByUid(uidClasses).observe(viewLifecycleOwner) {
            binding.tvClasses.text = "Class ${it?.nameClasses}" ?: "-"
        }

        binding.btnFinish.setOnClickListener {
            binding.pbYes.visibility = View.VISIBLE
            val dataUser = DataClassUser(
                nameUser = nameUser,
                uidMajor = uidMajor,
                uidLevel = uidLevel,
                uidClasses = uidClasses
            )
            vmUser.addUserData(dataUser).observe(viewLifecycleOwner) {
                when (it) {
                    "Success" -> {
                        if (fragmentContainer == R.id.startFragmentContainer) {
                            startActivity(Intent(requireActivity(), ActivityMain::class.java))
                            requireActivity().finish()
                            binding.pbYes.visibility = View.GONE
                        } else {
                            replaceFragmentWithoutBackStack(fragmentContainer, FragmentProfile())
                            binding.pbYes.visibility = View.GONE
                        }
                    }
                    else -> {
                        replaceToastFragment(toastContainer, FragmentToast(
                            R.drawable.fail,
                            "Something Went Wrong"
                        ))
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