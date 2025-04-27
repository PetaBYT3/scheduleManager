package com.schedule.rt.sync.fragment.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterDataSchedule
import com.schedule.rt.sync.databinding.FragmentDataMondayBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelSchedule

class FragmentDataMonday : Fragment() {

    private lateinit var binding: FragmentDataMondayBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvSchedule: AdapterDataSchedule

    private val vmLecturer: ViewModelLecturer by activityViewModels()
    private val vmSchedule: ViewModelSchedule by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDataMondayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView()
    }

    private fun recyclerView() {
        recyclerView = binding.rvMonday
        adapterRvSchedule = AdapterDataSchedule(vmLecturer, viewLifecycleOwner)
        recyclerView.adapter = adapterRvSchedule

        vmSchedule.getSchedule("monday").observe(viewLifecycleOwner) {
            adapterRvSchedule.updateRvSchedule(it)
            if (it.isNotEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvSchedule.setOnItemClickListener(object : AdapterDataSchedule.onItemClickListener {
            override fun onDeleteClick(position: Int) {
                DialogUtil.showBottomSheetConfirmation(
                    requireActivity(),
                    "Delete Schedule",
                    R.drawable.delete,
                    "Delete",
                    { bottomSheetBinding, bottomSheet ->

                        bottomSheetBinding.btnYes.setOnClickListener {
                            val uidCourse = adapterRvSchedule.dataClassCourse[position].uidCourse
                            vmSchedule.deleteSchedule(uidCourse).observe(viewLifecycleOwner) {
                                when (it) {
                                    "Success" -> {
                                        DialogUtil.showToast(requireActivity(), "Success", R.drawable.check)
                                        bottomSheet.dismiss()
                                    }
                                    "Fail" -> {
                                        DialogUtil.showToast(requireActivity(), "Fail", R.drawable.fail)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        })
    }
}