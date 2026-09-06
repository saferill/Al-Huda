package com.alhuda.app.core.util.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

object CallStateInspector {
    fun isCallActive(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        val telecom = context.getSystemService<TelecomManager>() ?: return false

        return telecom.isInCall
    }
}
