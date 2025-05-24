package com.schedule.rt.sync.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.schedule.rt.sync.databinding.FragmentInvalidUserBinding
import com.schedule.rt.sync.objectsingleton.TransitionUtil

class FragmentInvalidUser : Fragment() {

    private var _binding : FragmentInvalidUserBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentInvalidUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.post {
            TransitionUtil.slideUpTransition(binding.root)
        }

        binding.btnRestartApp.setOnClickListener {
            restartApp()
        }
    }

    private fun restartApp() {
        val intentRestart = requireActivity().packageManager.getLaunchIntentForPackage(requireActivity().packageName)
        intentRestart?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        // Akhiri semua aktivitas sebelumnya dan keluar dari proses
        requireActivity().finishAffinity()
        intentRestart?.let { startActivity(it) }

        // Pastikan proses aplikasi benar-benar dihentikan (opsional)
        System.exit(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}