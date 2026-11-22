package com.parksandcafe.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.parksandcafe.app.databinding.ActivityMainBinding
import com.parksandcafe.app.maps.MapsDetectAccessibilityService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val mapsPrefs by lazy {
        getSharedPreferences(MapsDetectAccessibilityService.PREFS, MODE_PRIVATE)
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val ok = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!ok) {
            Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_LONG).show()
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { refreshUi() }
                .addOnFailureListener {
                    Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show()
                }
        } catch (_: ApiException) {
            Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener { startGoogleSignIn() }
        binding.btnSignOut.setOnClickListener {
            auth.signOut()
            GoogleSignIn.getClient(this, googleOptions()).signOut()
            refreshUi()
        }
        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.btnMapsToggle.setOnClickListener {
            val enabled = mapsPrefs.getBoolean(
                MapsDetectAccessibilityService.KEY_MAPS_SAVE_ENABLED,
                true
            )
            mapsPrefs.edit()
                .putBoolean(MapsDetectAccessibilityService.KEY_MAPS_SAVE_ENABLED, !enabled)
                .apply()
            refreshUi()
        }
        binding.btnPreviewQuickSave.setOnClickListener {
            startActivity(Intent(this, QuickSaveActivity::class.java))
        }

        ensureLocationPermission()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun refreshUi() {
        val signedIn = auth.currentUser != null
        binding.status.setText(
            if (signedIn) R.string.main_signed_in else R.string.main_signed_out
        )
        binding.btnSignIn.visibility = if (signedIn) View.GONE else View.VISIBLE
        binding.btnSignOut.visibility = if (signedIn) View.VISIBLE else View.GONE

        val mapsOn = mapsPrefs.getBoolean(
            MapsDetectAccessibilityService.KEY_MAPS_SAVE_ENABLED,
            true
        )
        binding.btnMapsToggle.setText(
            if (mapsOn) R.string.maps_save_toggle_on else R.string.maps_save_toggle_off
        )
    }

    private fun ensureLocationPermission() {
        val fine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fine) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    private fun googleOptions(): GoogleSignInOptions {
        val webClientId = getString(R.string.default_web_client_id_placeholder)
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    private fun startGoogleSignIn() {
        val client = GoogleSignIn.getClient(this, googleOptions())
        googleSignInLauncher.launch(client.signInIntent)
    }
}
