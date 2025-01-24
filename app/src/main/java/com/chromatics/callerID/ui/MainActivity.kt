package com.chromatics.callerID.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.chromatics.callerID.R
import com.chromatics.callerID.databinding.ActivityMainBinding
import com.chromatics.callerID.utils.AppUtils
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.navView

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)

        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    navController.navigate(R.id.navigation_dashboard)
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_notes -> {
                    navController.navigate(R.id.navigation_notes)
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_notification -> {
                    navController.navigate(R.id.navigation_notification)
                    return@setOnItemSelectedListener true
                }

                else -> return@setOnItemSelectedListener false
            }
        }
    }
    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.navigation_dashboard) {
            AppUtils.customAlertDialog(
                this,
                getString(R.string.confirm_exit),
                getString(R.string.exit_body),
                false
            )
                .setPositiveButton(getString(R.string.yes)) { _, _ -> finish() }
                .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            super.onBackPressed()
        }
    }

}