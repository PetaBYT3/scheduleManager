package com.schedule.rt.sync.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.schedule.rt.sync.fragment.FragmentSplash
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.ActivityStartBinding
import com.schedule.rt.sync.viewmodel.ViewModelUser

class ActivityStart : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private val vmUser: ViewModelUser by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportFragmentManager.commit {
            replace(R.id.startFragmentContainer, FragmentSplash())
        }
    }

    override fun onStart() {
        super.onStart()

    }
}