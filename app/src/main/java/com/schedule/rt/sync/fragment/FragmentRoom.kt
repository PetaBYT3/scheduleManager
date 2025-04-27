package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterRoom
import com.schedule.rt.sync.databinding.FragmentRoomBinding
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelRoom
import com.schedule.rt.sync.viewmodel.ViewModelSchedule

class FragmentRoom : Fragment() {

    private lateinit var binding: FragmentRoomBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvRoom: AdapterRoom
    private val viewModelAdministrator: ViewModelAdministrator by activityViewModels()

    private val vmBuilding: ViewModelBuilding by activityViewModels()
    private val vmRoom: ViewModelRoom by activityViewModels()
    private val vmSchedule: ViewModelSchedule by activityViewModels()

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
        binding = FragmentRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.post {
            TransitionUtil.slideUpTransition(binding.nestedScrollView)
        }

        rvRoom()

        actionBar()
    }

    private fun actionBar() {
        val uidBuilding = vmRoom.uidBuilding
        vmBuilding.getBuildingByUid(uidBuilding).observe(viewLifecycleOwner) {
            binding.clToolBar.title = buildString {
                append("Building ")
                append(it?.nameBuilding)
            }
        }

        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun rvRoom() {
        recyclerView = binding.rvRoom
        adapterRvRoom = AdapterRoom()
        recyclerView.adapter = adapterRvRoom

        vmRoom.getRoom()
        vmRoom.dataRoom.observe(viewLifecycleOwner) {
            adapterRvRoom.updateRvRoom(it)
            if (it.isNotEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvRoom.setOnItemClickListener(object : AdapterRoom.onItemClickListener {
            override fun onItemClick(position: Int) {
                val uidRoom = adapterRvRoom.dataClassRoom[position].uidRoom

                vmRoom.uidRoom = uidRoom
                vmSchedule.uidRoom = uidRoom

                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.fragmentContainer, FragmentDataDay::class.java, null)
                    addToBackStack(null)
                }
            }
        })
    }
}