package com.chromatics.caller_id.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.chromatics.caller_id.R
import com.chromatics.caller_id.databinding.ActivityMainBinding
import com.chromatics.caller_id.utils.AppUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Request codes for multiple permissions
    private val REQUEST_CODE_MULTIPLE_PERMISSIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)

        // Check and request permissions
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        val permissions = arrayOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.READ_CALL_LOG,
        )

        // Check each permission and add to the list if not granted
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission)
            }
        }

        // Request permissions if any are not granted
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                REQUEST_CODE_MULTIPLE_PERMISSIONS
            )
        } else {
            // All permissions granted, proceed with the functionality
            Log.d("MainActivity", "All permissions granted.")
//            proceedWithAppFeatures()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_MULTIPLE_PERMISSIONS -> {
                val deniedPermissions = mutableListOf<String>()
                for (i in permissions.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i])
                    }
                }

                if (deniedPermissions.isEmpty()) {
                    // All permissions granted
                    Log.d("MainActivity", "All requested permissions granted.")
//                    proceedWithAppFeatures()
                } else {
                    // Some permissions denied, show a message or handle accordingly
                    Toast.makeText(
                        this,
                        "Some permissions were denied. Please enable them in settings.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}


