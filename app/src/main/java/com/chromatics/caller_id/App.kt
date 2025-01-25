package com.chromatics.caller_id

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.chromatics.caller_id.common.CallMonitoringService
import com.chromatics.caller_id.common.Config
import com.chromatics.caller_id.common.DeviceProtectedStorageMigrator
import com.chromatics.caller_id.utils.DebuggingUtils
import com.chromatics.caller_id.utils.Settings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

  companion object {
    @SuppressLint("StaticFieldLeak")
    private var instance: App? = null

    @SuppressLint("StaticFieldLeak")
    private var settings: Settings? = null

    @JvmStatic
    fun getInstance(): App? = instance

    @JvmStatic
    fun getSettings(): Settings? = settings

    fun setUiMode(uiMode: Int) {
      AppCompatDelegate.setDefaultNightMode(uiMode)
    }
  }

  override fun onCreate() {
    super.onCreate()
    instance = this

    DebuggingUtils.setUpCrashHandler()

    DeviceProtectedStorageMigrator().migrate(this)

    settings = Settings(getDeviceProtectedStorageContext()).apply {
      init()
    }

    Config.init(getDeviceProtectedStorageContext(), settings!!)

    setUiMode(settings!!.uiMode)

    if (settings!!.useMonitoringService) {
      CallMonitoringService.start(this)
    }
  }

  private fun getDeviceProtectedStorageContext(): Context {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      createDeviceProtectedStorageContext()
    } else {
      this
    }
  }
}
