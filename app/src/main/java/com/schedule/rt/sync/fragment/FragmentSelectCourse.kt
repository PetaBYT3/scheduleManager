package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.adapter.AdapterCourse
import com.schedule.rt.sync.databinding.FragmentSelectCourseBinding
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.objectsingleton.TransitionUtil
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelRoom

class FragmentSelectCourse : Fragment() {

    private var _binding: FragmentSelectCourseBinding? = null
    private val binding get() = _binding!!

    private val vmCourse : ViewModelCourse by activityViewModels()
    private val vmLecturer : ViewModelLecturer by activityViewModels()
    private val vmMajor : ViewModelMajor by activityViewModels()
    private val vmLevel : ViewModelLevel by activityViewModels()
    private val vmClasses : ViewModelClasses by activityViewModels()
    private val vmBuilding : ViewModelBuilding by activityViewModels()
    private val vmRoom : ViewModelRoom by activityViewModels()

    var onViewCreated: ((FragmentSelectCourseBinding) -> Unit)? = null
    var onAddClick: ((DataClassCourse) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSelectCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onViewCreated?.invoke(binding)

        rootMaxHeight()
    }

    private fun rootMaxHeight() {
        val displayMetrics = requireActivity().windowManager.currentWindowMetrics
        val insets = displayMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        val fullScreen = displayMetrics.bounds.height() - insets.bottom - insets.top
        val halfScreen = fullScreen / 2
        binding.root.layoutParams.height = halfScreen
        binding.root.requestLayout()

        var isFullScreen = false

        binding.btnFullscreen.setOnClickListener {
            val currentHeight = binding.root.height
            if (isFullScreen) {
                TransitionUtil.animateHeight(binding.root, currentHeight, halfScreen)
                binding.ivFullscreen.setImageResource(R.drawable.fullscreen)
                isFullScreen = false
            } else {
                TransitionUtil.animateHeight(binding.root, currentHeight, fullScreen)
                binding.ivFullscreen.setImageResource(R.drawable.close_fullscreen)
                isFullScreen = true
            }
        }
    }

    fun rvCourseByClasses() {
        val recyclerView: RecyclerView = binding.rvCourse
        val adapter = AdapterCourse(
            vmLecturer,
            vmMajor,
            vmLevel,
            vmClasses,
            vmCourse,
            vmBuilding,
            vmRoom,
            viewLifecycleOwner,
            tvData1 = true,
            tvData3 = true,
            tvData4 = true,
            tvData5 = true,
            addSchedule = true,
            btnFirst = false,
            btnSecond = false,
            onNextClick = {
                onAddClick?.invoke(it)
            }
        )
        recyclerView.adapter = adapter

        vmCourse.getCourse().observe(viewLifecycleOwner) {
            adapter.updateData(it)
            if (it.isNotEmpty()) {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.GONE
            } else {
                binding.pbRv.visibility = View.GONE
                binding.layoutNoData.visibility = View.VISIBLE
            }
        }
    }

    fun rvCourseByLecturer(uidLecturer: String?) {
        val recyclerView: RecyclerView = binding.rvCourse
        val adapter = AdapterCourse(
            vmLecturer,
            vmMajor,
            vmLevel,
            vmClasses,
            vmCourse,
            vmBuilding,
            vmRoom,
            viewLifecycleOwner,
            tvData1 = true,
            tvData2 = true,
            tvData4 = true,
            tvData5 = true,
            addSchedule = true,
            btnFirst = false,
            btnSecond = false,
            onNextClick = {
                onAddClick?.invoke(it)
            }
        )
        recyclerView.adapter = adapter

        vmCourse.getCourseByLecturer(uidLecturer).observe(viewLifecycleOwner) {
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