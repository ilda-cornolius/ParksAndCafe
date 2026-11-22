package com.parksandcafe.app.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.parksandcafe.app.MainActivity
import com.parksandcafe.app.PlaceType
import com.parksandcafe.app.R
import com.parksandcafe.app.data.PlaceDraft
import com.parksandcafe.app.data.PlaceRepository
import com.parksandcafe.app.location.LocationHelper
import kotlinx.coroutines.runBlocking

/**
 * Handles widget taps: location → Firestore write.
 * Uses JobIntentService so work continues briefly after the broadcast returns.
 */
class SavePlaceService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val typeName = intent.getStringExtra(EXTRA_TYPE) ?: return
        val type = PlaceType.valueOf(typeName)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            showToast(getString(R.string.error_auth))
            openApp()
            return
        }

        val locationHelper = LocationHelper(this)
        if (!locationHelper.hasPermission()) {
            showToast(getString(R.string.permission_needed))
            openApp()
            return
        }

        showToast(
            if (type == PlaceType.PARK) getString(R.string.saving_park)
            else getString(R.string.saving_cafe)
        )

        try {
            runBlocking {
                val location = locationHelper.currentLocation()
                PlaceRepository().savePlace(
                    PlaceDraft(
                        lat = location.latitude,
                        lng = location.longitude,
                        type = type,
                    )
                )
            }
            val msg = if (type == PlaceType.PARK) {
                getString(R.string.saved_park)
            } else {
                getString(R.string.saved_cafe)
            }
            showToast(msg)
            notifySaved(msg)
        } catch (_: Exception) {
            showToast(getString(R.string.error_save))
        }
    }

    private fun showToast(message: String) {
        // Toasts from background: post to main looper
        android.os.Handler(mainLooper).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openApp() {
        val launch = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(launch)
    }

    private fun notifySaved(message: String) {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Saves",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_park)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(message)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val JOB_ID = 1001
        private const val EXTRA_TYPE = "place_type"
        private const val CHANNEL_ID = "parks_cafe_saves"
        private const val NOTIFICATION_ID = 42

        fun enqueue(context: Context, type: PlaceType) {
            val intent = Intent(context, SavePlaceService::class.java).apply {
                putExtra(EXTRA_TYPE, type.name)
            }
            enqueueWork(context, SavePlaceService::class.java, JOB_ID, intent)
        }
    }
}
