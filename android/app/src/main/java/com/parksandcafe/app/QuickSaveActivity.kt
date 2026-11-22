package com.parksandcafe.app

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.parksandcafe.app.databinding.ActivityQuickSaveBinding
import com.parksandcafe.app.widget.SavePlaceService

/**
 * Translucent Park/Cafe sheet shown over Maps (or launched standalone).
 * Uses an activity overlay rather than a persistent SYSTEM_ALERT_WINDOW bubble
 * so the user sees a clear, dismissible UI without a constant floating window.
 */
class QuickSaveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuickSaveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityQuickSaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.quickSaveRoot.setOnClickListener { finish() }
        binding.btnDismiss.setOnClickListener { finish() }

        binding.btnPark.setOnClickListener { save(PlaceType.PARK) }
        binding.btnCafe.setOnClickListener { save(PlaceType.CAFE) }
    }

    private fun save(type: PlaceType) {
        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show()
            startActivity(android.content.Intent(this, MainActivity::class.java))
            finish()
            return
        }
        SavePlaceService.enqueue(this, type)
        finish()
    }

    companion object {
        const val EXTRA_SOURCE = "source"
        const val SOURCE_MAPS = "maps"
    }
}
