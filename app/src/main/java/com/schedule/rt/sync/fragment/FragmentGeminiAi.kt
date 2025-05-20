package com.schedule.rt.sync.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.schedule.rt.sync.R
import com.schedule.rt.sync.activity.ActivityMain
import com.schedule.rt.sync.databinding.FragmentGeminiAiBinding
import com.schedule.rt.sync.objectsingleton.DialogUtil.showToastFragment
import com.schedule.rt.sync.viewmodel.ViewModeGeminiAI

class FragmentGeminiAi : Fragment() {

    private var _binding : FragmentGeminiAiBinding? = null
    private val binding get() = _binding!!

    private val vmGemini: ViewModeGeminiAI by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGeminiAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootMaxHeight()

        (requireActivity() as ActivityMain).btnGeminiAi(false)

        binding.toolBar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.commit {
                remove(this@FragmentGeminiAi)
            }
        }

        binding.clearLastAnswer.setOnClickListener {
            vmGemini.clearQuestionResult()
        }

        binding.scheduleContext.setOnClickListener {
            it.isSelected = !it.isSelected
            binding.scheduleContext.setBackgroundResource(
                if (it.isSelected) {
                    R.drawable.bg_button
                } else {
                    android.R.color.transparent
                }
            )
        }

        binding.btnSendPrompt.setOnClickListener {
            val prompt = binding.etPrompt.text.toString()
            if (prompt.isNotEmpty()) {
                binding.layoutNoData.visibility = View.GONE
                binding.pbGeminiAI.visibility = View.VISIBLE
                binding.tvResponse.visibility = View.GONE
                vmGemini.answerCustomQuestion(prompt, binding.scheduleContext.isSelected)
                binding.etPrompt.text?.clear()
            } else {
                showToastFragment(FragmentToast(
                    R.drawable.fail,
                    "Ask Something"
                ))
            }
        }

        vmGemini.processResult.observe(viewLifecycleOwner) {
            when (it) {
                "Success" -> {
                    binding.pbGeminiAI.visibility = View.GONE
                    binding.tvResponse.visibility = View.VISIBLE
                }
            }
        }

        vmGemini.questionResult.observe(viewLifecycleOwner) {
            binding.tvResponse.text = it

            if (it.isNullOrEmpty()) {
                binding.layoutNoData.visibility = View.VISIBLE
            } else {
                binding.layoutNoData.visibility = View.GONE
            }
        }
    }

    private fun rootMaxHeight() {
        val displayMetrics = requireActivity().windowManager.currentWindowMetrics
        val insets = displayMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        val fullScreen = displayMetrics.bounds.height() - insets.bottom - insets.top
        val halfScreen = fullScreen / 2
        binding.root.layoutParams.height = halfScreen
        binding.root.requestLayout()

        var isFullScreen = false

//        binding.btnFullscreen.setOnClickListener {
//            val currentHeight = binding.root.height
//            if (isFullScreen) {
//                TransitionUtil.animateHeight(binding.root, currentHeight, halfScreen)
//                binding.ivFullscreen.setImageResource(R.drawable.fullscreen)
//                isFullScreen = false
//            } else {
//                TransitionUtil.animateHeight(binding.root, currentHeight, fullScreen)
//                binding.ivFullscreen.setImageResource(R.drawable.close_fullscreen)
//                isFullScreen = true
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as ActivityMain).btnGeminiAi(true)
        _binding = null
    }
}