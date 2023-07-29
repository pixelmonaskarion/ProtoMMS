package com.chrissytopher.pumpmessager

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.core.content.ContextCompat

abstract class BaseActivity : ComponentActivity() {
    abstract fun startApp();

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted: Map<String,Boolean> ->
            if (isGranted.containsValue(false)) {
                Log.e("ProtoMMS", isGranted.toString())
                setContent {
                    Text(text = "was denied")
                }
            } else {
                startApp()
            }
        }

    private fun hasPermission(context: Context, permissionStr: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permissionStr
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissions(permissions: ArrayList<String>, context: Context) {
        val notGrantedPermissions = ArrayList<String>()
        while (permissions.isNotEmpty()) {
            val permission = permissions[permissions.size-1]
            permissions.removeAt(permissions.size-1)
            if (!hasPermission(context, permission)) {
                notGrantedPermissions.add(permission)
            }
        }
        requestPermissionLauncher.launch(notGrantedPermissions.toArray(arrayOf()))
    }

    fun askPermissions() {
        val permissions = arrayListOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_NUMBERS)
        checkPermissions(permissions, applicationContext)
        var allPermissionsAllowed = true
        for (permission in permissions) {
            if (!hasPermission(applicationContext, permission)) {
                allPermissionsAllowed = false
            }
        }
        if (allPermissionsAllowed) {
            startApp()
        } else {
            Log.e("ProtoMMS", "not all permissions were allowed")
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init(contentResolver, this)
        val roleManager = getSystemService(RoleManager::class.java)
        if (roleManager!!.isRoleAvailable(RoleManager.ROLE_SMS)) {
            if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                askPermissions()
            } else {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                val startForResult = registerForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result: ActivityResult ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        askPermissions()
                    } else {
                        Log.e("ProtoMMS", "result code: ${result.resultCode}")
                        finish()
                    }
                }

                startForResult.launch(intent)
            }
        } else {
            Log.e("ProtoMMS", "role is not available")
            finish()
        }
    }
}