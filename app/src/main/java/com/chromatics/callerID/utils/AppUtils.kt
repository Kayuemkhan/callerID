package com.chromatics.callerID.utils

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.os.Build.VERSION
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random
import android.provider.Settings.Secure;
import android.util.Log
import com.chromatics.callerID.R
import java.util.UUID

object AppUtils {
  val phoneStatePermisison = arrayOf(permission.READ_PHONE_STATE)

  val currentDate: String
    get() {
      val c = Calendar.getInstance().time
      val dateFormat = SimpleDateFormat("EEEE dd, MMMM", Locale.getDefault())
      return dateFormat.format(c)
    }

  fun currentTimeInMilliSec(): Long {
    return System.currentTimeMillis()
  }

  fun getDateFromMillisec(millis: Long): String {
    val d = Date()
    d.time = millis
    val dateFormat: DateFormat = SimpleDateFormat("EEEE dd, MMMM", Locale.getDefault())
    return dateFormat.format(d)
  }


  fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
      (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
  }

  fun message(view: View?, msg: String?, textColor: Int, backgroundColor: Int) {
    Log.d("ErrorSnackCalled","Error Snack is showing")
    if (view == null) return
    val snack = Snackbar.make(view, msg!!, Snackbar.LENGTH_SHORT)
    val snackBarView = snack.view
    snackBarView.setBackgroundColor(backgroundColor)
    val snackBarText =
      snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    snackBarText.setTextColor(textColor)
    snack.show()
  }

  fun showLoadingDialog(context: Context): Dialog {
    val progressDialog = Dialog(context)
    if (progressDialog.window != null) {
      progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    progressDialog.setContentView(R.layout.progress_dialog)
    progressDialog.setCancelable(false)
    progressDialog.setCanceledOnTouchOutside(false)
    return progressDialog
  }

  fun customAlertDialog(
    context: Context,
    title: String,
    body: String,
    isCancelable: Boolean
  ): AlertDialog.Builder {
    return AlertDialog.Builder(context)
      .setTitle(title)
      .setCancelable(isCancelable)
      .setMessage(body)
  }

  fun generateUUID(): String {
    return UUID.randomUUID().toString()
  }



  @SuppressLint("ObsoleteSdkInt")
  fun hasPermissionList(permissionList: Array<String>, context: Context): Boolean {
    var res = true
    for (permission in permissionList) {
      if (!hasPermission(permission, context)) {
        res = false
      }
    }
    if (VERSION.SDK_INT < 22) {
      res = true
    }
    return res
  }

  private fun hasPermission(permission: String, context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(context, permission) == 0
  }

  @SuppressLint("HardwareIds")
  fun getDeviceId(context: Context): String {
    return Secure.getString(context.contentResolver, Secure.ANDROID_ID)
  }
}