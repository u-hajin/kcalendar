package com.kkwakjavacoding.kcalendar.firebase

import android.annotation.SuppressLint
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.os.Build
import java.util.*

object UserID {
    @SuppressLint("MissingPermission", "HardwareIds")
    fun getWidevineID(): String {
        val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)

        val wvDrm = try {
            MediaDrm(WIDEVINE_UUID)
        } catch (e: UnsupportedSchemeException) {
            null
        }
        val widevineId = wvDrm!!.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
        val encodedWidevineId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(widevineId).trim()
        } else {
            "null"
        }

        return encodedWidevineId.replace("/", "")
    }
}