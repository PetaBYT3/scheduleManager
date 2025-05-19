package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterRoom
import com.schedule.rt.sync.databinding.FragmentRoomBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.replaceFragmentWithBackStack
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelRoom
import com.schedule.rt.sync.viewmodel.ViewModelSchedule

class FragmentRoom(
    var btnAddSchedule: Boolean? = null,
    var btnEditSchedule: Boolean? = null
) : Fragment() {

    private var _binding: FragmentRoomBinding? = null
    private val binding get() = _binding!!

    private val vmBuilding: ViewModelBuilding by activityViewModels()
    private val vmRoom: ViewModelRoom by activityViewModels()
    private val vmSchedule: ViewModelSchedule by activityViewModels()
    private val vmCourse: ViewModelCourse by activityViewModels()

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
        _binding = FragmentRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        val uidBuilding = vmRoom.uidBuildingReference.value
        vmBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
            binding.clToolBar.title = buildString {
                append("Building ")
                append(it?.nameBuilding)
            }
        }

        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        rvRoom()

    }

    private fun rvRoom() {
        val recyclerView: RecyclerView = binding.rvRoom
        val adapter = AdapterRoom(
            lifecycleOwner = viewLifecycleOwner,
            vmCourse = vmCourse,
            tvData1 = true,
            btnFirst = false,
            btnSecond = false,
            btnNext = true,
            onNextClick = {
                val uidRoom = it.uidRoom
                vmSchedule.uidRoomReference(uidRoom)
                val fragmentDataSchedule = FragmentDataSchedule(btnAddSchedule = btnAddSchedule, btnEditSchedule = btnEditSchedule)
                replaceFragmentWithBackStack(R.id.mainFragmentContainer, fragmentDataSchedule, null)
            }
        )

        recyclerView.adapter = adapter

        vmRoom.getRoom().observe(viewLifecycleOwner) {
            adapter.updateData(it)
            if (it.isNullOrEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}