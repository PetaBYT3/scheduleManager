package com.schedule.rt.sync.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.ActivityMainBinding
import com.schedule.rt.sync.fragment.FragmentAdministrator
import com.schedule.rt.sync.fragment.FragmentDataLevel
import com.schedule.rt.sync.fragment.FragmentHome
import com.schedule.rt.sync.fragment.FragmentNotification
import com.schedule.rt.sync.objectsingleton.DialogUtil
import com.schedule.rt.sync.viewmodel.ViewModelAdministrator
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelScheduleManager
import com.schedule.rt.sync.viewmodel.ViewModelUser

class ActivityMain : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var drawerLayout: DrawerLayout

    private val viewModelUser : ViewModelUser by viewModels()
    private val viewModelScheduleManager : ViewModelScheduleManager by viewModels()
    private val viewModelAdministrator: ViewModelAdministrator by viewModels()
    private val viewModelLevel: ViewModelLevel by viewModels()

    private var backPressedOnce = false
    private val handler = Handler(Looper.getMainLooper())

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
                setReorderingAllowed(true)
                replaceFragment(FragmentHome(), R.id.btnHome)
            }
        }

        firebaseAuth = FirebaseAuth.getInstance()

        drawerLayout = binding.dlActivityMain
        val navigationView: NavigationView = binding.navigationView
        val headerView = navigationView.getHeaderView(0)
        val btnCloseDrawer: ImageView = headerView.findViewById(R.id.btnCloseDrawer)

        val menu = navigationView.menu
        val btnAdministrator = menu.findItem(R.id.btnAdministrator)
        val btnScheduleManager = menu.findItem(R.id.btnScheduleManager)

        viewModelUser.dataLecturer.observe(this) {
            if (it != null) {
                viewModelScheduleManager.uidMajor = it.uidMajorManager
                viewModelAdministrator.uidMajorSchedule = it.uidMajorManager
                viewModelLevel.uidMajor = it.uidMajorManager

                if (it.administratorAccess == "True") {
                    btnAdministrator.isVisible = true
                    viewModelScheduleManager.getMajorByUid().observe(this) {
                        btnScheduleManager.title = it?.nameMajor
                    }
                } else if (it.administratorAccess == "False") {
                    btnAdministrator.isVisible = false
                }

                if (it.uidMajorManager != null) {
                    btnScheduleManager.isVisible = true
                } else if (it.uidMajorManager == null) {
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
                R.id.btnNotification -> {
                    if (!isCurrentFragment(FragmentNotification::class.java)) {
                        replaceFragment(FragmentNotification(), R.id.btnNotification)
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
                R.id.btnSignOut -> {
                    DialogUtil.showBottomSheetConfirmation(
                        this,
                        "Sign Out",
                        R.drawable.signout,
                        "Sign Out",
                        { bottomSheetBinding, bottomSheet ->

                            bottomSheetBinding.tvTittle.text = buildString {
                                append("Are You Sure You Want To Sign Out?")
                            }

                            bottomSheetBinding.btnYes.setOnClickListener {
                                firebaseAuth.signOut()
                                startActivity(Intent(this, ActivityStart::class.java))
                                finish()
                            }
                        }
                    )
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmentManager = supportFragmentManager
                if (fragmentManager.backStackEntryCount > 0) {
                    fragmentManager.popBackStack()
                } else {
                    if (backPressedOnce) {
                        finish()
                    } else {
                        backPressedOnce = true
                        DialogUtil.showToast(
                            context = this@ActivityMain,
                            message = "Press Again To Exit",
                            icon = R.drawable.exit
                        )
                        handler.postDelayed({ backPressedOnce = false }, 2000)
                    }
                }
            }
        })
    }

    fun btnDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun replaceFragment(fragment: Fragment, menuItemUid: Int? = null) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
            commit()
        }

        if (menuItemUid != null) {
            binding.navigationView.setCheckedItem(menuItemUid)
        }
    }

    private fun isCurrentFragment(fragmentClass: Class<out Fragment>): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        return currentFragment != null && currentFragment::class.java == fragmentClass
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}