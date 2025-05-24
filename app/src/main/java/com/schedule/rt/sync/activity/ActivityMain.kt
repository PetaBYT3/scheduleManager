package com.schedule.rt.sync.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.ActivityMainBinding
import com.schedule.rt.sync.fragment.FragmentAdministrator
import com.schedule.rt.sync.fragment.FragmentDataLevel
import com.schedule.rt.sync.fragment.FragmentGeminiAi
import com.schedule.rt.sync.fragment.FragmentHome
import com.schedule.rt.sync.fragment.FragmentInvalidUser
import com.schedule.rt.sync.fragment.FragmentProfile
import com.schedule.rt.sync.fragment.FragmentSettings
import com.schedule.rt.sync.service.ForegroundService
import com.schedule.rt.sync.userpreferences.SettingsPreferences
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelUser
import kotlinx.coroutines.launch

class ActivityMain : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var drawerLayout: DrawerLayout

    private val vmUser : ViewModelUser by viewModels()
    private val vmLecturer : ViewModelLecturer by viewModels()
    private val vmMajor : ViewModelMajor by viewModels()
    private val vmLevel: ViewModelLevel by viewModels()
    private val vmClass: ViewModelClasses by viewModels()
    private val vmCourse: ViewModelCourse by viewModels()

    private val settingsPreferences = SettingsPreferences(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dlActivityMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replaceFragment(FragmentHome(), R.id.btnHome)
            }
        }

        binding.btnGeminiAi.imageTintList = null
        binding.btnGeminiAi.setOnClickListener {
            supportFragmentManager.commit {
                add(R.id.mainBottomSheetContainer, FragmentGeminiAi())
            }
        }

        foregroundService()

        drawerLayout = binding.dlActivityMain
        val navigationView: NavigationView = binding.navigationView
        val headerView = navigationView.getHeaderView(0)
        val btnCloseDrawer: MaterialCardView = headerView.findViewById(R.id.btnCloseDrawer)

        val menu = navigationView.menu
        val btnAdministrator = menu.findItem(R.id.btnAdministrator)
        val btnScheduleManager = menu.findItem(R.id.btnScheduleManager)

        vmUser.getUser().observe(this) {
            val uidMajor = it?.uidMajor
            val uidLevel = it?.uidLevel
            val uidClasses = it?.uidClasses
            val uidLecturer = it?.uidLecturer

            val isLecturerInvalid = uidLecturer == null
            val isStudentInvalid = uidMajor == null && uidLevel == null && uidClasses == null

            if (isLecturerInvalid && isStudentInvalid) {
                supportFragmentManager.commit {
                    replace(R.id.mainFragmentContainer, FragmentInvalidUser())
                }
            } else {
                val uidLecturer = it.uidLecturer
                if (uidLecturer != null) {
                    vmLecturer.getLecturerByUid(uidLecturer).observe(this) {
                        val uidMajor = it?.uidMajorManager
                        val administratorAccess = it?.administratorAccess.toString()

                        if (administratorAccess == "True") {
                            btnAdministrator.isVisible = true
                        } else {
                            btnAdministrator.isVisible = false
                        }

                        if (uidMajor != null) {
                            vmMajor.getMajorByUid(uidMajor).observe(this) {
                                vmLevel.uidMajorReference(uidMajor)
                                vmClass.uidMajorReference(uidMajor)
                                vmCourse.uidMajorReference(uidMajor)

                                btnScheduleManager.title = it?.nameMajor
                                btnScheduleManager.isVisible = true
                            }
                        } else {
                            btnScheduleManager.isVisible = false
                        }
                    }
                } else {
                    btnAdministrator.isVisible = false
                    btnScheduleManager.isVisible = false
                }
            }
        }

        btnCloseDrawer.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.btnHome -> {
                    if (!isCurrentFragment(FragmentHome::class.java)) {
                        replaceFragment(FragmentHome(), R.id.btnHome)
                    }
                }
                R.id.btnSettings -> {
                    if (!isCurrentFragment(FragmentSettings::class.java)) {
                        replaceFragment(FragmentSettings(), R.id.btnSettings)
                    }
                }
                R.id.btnProfile -> {
                    if (!isCurrentFragment(FragmentProfile::class.java)) {
                        replaceFragment(FragmentProfile(), R.id.btnProfile)
                    }
                }
                R.id.btnAdministrator -> {
                    if (!isCurrentFragment(FragmentAdministrator::class.java)) {
                        replaceFragment(FragmentAdministrator(), R.id.btnAdministrator)
                    }
                }
                R.id.btnScheduleManager -> {
                    if (!isCurrentFragment(FragmentDataLevel::class.java)) {
                        replaceFragment(FragmentDataLevel(), R.id.btnScheduleManager)
                    }
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    fun btnDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    fun btnGeminiAi(visibility: Boolean) {
        if (visibility) {
            binding.btnGeminiAi.visibility = View.VISIBLE
        } else {
            binding.btnGeminiAi.visibility = View.GONE
        }
    }

    fun replaceFragment(fragment: Fragment, menuItemUid: Int? = null) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.mainFragmentContainer, fragment)
            commit()
        }

        if (menuItemUid != null) {
            binding.navigationView.setCheckedItem(menuItemUid)
        }
    }

    private fun isCurrentFragment(fragmentClass: Class<out Fragment>): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.mainFragmentContainer)
        return currentFragment != null && currentFragment::class.java == fragmentClass
    }

    private fun foregroundService() {
        val serviceIntent = Intent(this@ActivityMain, ForegroundService::class.java)
        lifecycleScope.launch {
            settingsPreferences.getForegroundServiceStatus.collect { foregroundStatus ->
                if (foregroundStatus == true) {
                    startForegroundServiceNow(serviceIntent)
                } else {
                    stopService(serviceIntent)
                }
            }
        }
    }

    private fun startForegroundServiceNow(serviceIntent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}