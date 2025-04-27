package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.schedule.rt.sync.adapter.AdapterCourse
import com.schedule.rt.sync.databinding.BottomSheetCourseBinding
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelCourse

class BottomSheetCourse : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetCourseBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterRvCourse: AdapterCourse

    private val viewModelAdministrator : ViewModelAdministrator by activityViewModels()

    private val vmCourse : ViewModelCourse by activityViewModels()

    private var clickListener: onClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = BottomSheetCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCourse()

        actionBar()
    }

    private fun actionBar() {
        binding.toolBar.setNavigationOnClickListener {
            dismiss()
        }

        binding.toolBar.title = "Course"
    }

    private fun rvCourse() {
        recyclerView = binding.rvCourse
        adapterRvCourse = AdapterCourse()
        recyclerView.adapter = adapterRvCourse

        vmCourse.getCourse()
        vmCourse.dataCourse.observe(viewLifecycleOwner) {
            adapterRvCourse.updateRvCourse(it)
            if (it.isNotEmpty()) {
                binding.pbRvCourse.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRvCourse.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }

        adapterRvCourse.setOnItemClickListener(object : AdapterCourse.onItemClickListener {
            override fun onAddClick(position: Int) {
                clickListener?.onAddClick(position, adapterRvCourse)
            }
        })
    }
    interface onClickListener {
        fun onAddClick(position: Int, adapterRvCourse: AdapterCourse)
    }

    fun setOnClickListener(clickListener: onClickListener) {
        this.clickListener = clickListener
    }
}