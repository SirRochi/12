package com.example.a1012

import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.MainScope
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var parkButton: Button
    private lateinit var exitButton: Button
    private lateinit var locationManager: LocationManager
    private var parkedLocation: Location? = null
    private var timer: CountDownTimer? = null
    private val durationMillis: Long = 30 * 60 * 1000 // 30 minutes
    private val LOCATION_PERMISSION_REQUEST_CODE = 0
    private val mainScope = MainScope()
    private var endTimeMillis: Long = 0
    private lateinit var endTimeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialization of endTimeTextView
        endTimeTextView = findViewById(R.id.timeRemainingTextView)

        val setDurationButton: Button = findViewById(R.id.setDurationButton)

        setDurationButton.setOnClickListener {
            showTimePickerDialog()
        }

        // Check if the device is running Android 11 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            // For devices below Android 11
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_VISIBLE
        }

        // Initialize other variables
        parkButton = findViewById(R.id.parkButton)
        exitButton = findViewById(R.id.exitButton)

        // Initialize the LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Request ACCESS_FINE_LOCATION permissions, make sure to have the permission in the manifest
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Add listener to get location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } else {
            // Request permissions if not already granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Explain to the user why you need the permission
                // You can use an AlertDialog or another form of message
                // to provide a more descriptive explanation.
                showDialogWithExplanation()
            } else {
                // Request permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Update the current location of the user
            val latitude = location.latitude
            val longitude = location.longitude
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Handle status changes if needed
        }

        override fun onProviderEnabled(provider: String) {
            // Called when the user enables the location provider
        }

        override fun onProviderDisabled(provider: String) {
            // Called when the user disables the location provider
        }
    }
//pilsante durata
private fun showTimePickerDialog() {
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)

    val setDurationButton: Button = findViewById(R.id.setDurationButton)
    val endTimeTextView: TextView = findViewById(R.id.timeRemainingTextView)

    val timePickerDialog = TimePickerDialog(
        this,
        { _, hourOfDay, minute ->
            // Calcola la durata in millisecondi
            val selectedDuration = (hourOfDay * 60 + minute) * 60 * 1000
            // Aggiorna endTimeMillis con il timestamp dell'ora di sosta selezionata
            endTimeMillis = System.currentTimeMillis() + selectedDuration
            // Avvia il timer
            startTimer(selectedDuration)

            // Aggiorna il testo del pulsante con l'orario selezionato
            val buttonText = String.format(Locale.getDefault(), "Durata: %02d:%02d", hourOfDay, minute)
            setDurationButton.text = buttonText

            // Aggiorna endTimeTextView con l'orario selezionato
            val endTimeText = String.format(Locale.getDefault(), "Tempo rimanente: %02d:%02d", hourOfDay, minute)
            endTimeTextView.text = endTimeText
            endTimeTextView.visibility = View.VISIBLE
        },
        currentHour,
        currentMinute,
        true
    )

    timePickerDialog.show()
}

    private fun showDialogWithExplanation() {
        // Implement the code to show an explanation when requesting permissions
    }

    private fun startTimer(durationMillis: Int) {
        if (timer == null) {
            endTimeMillis = System.currentTimeMillis() + durationMillis

            timer = object : CountDownTimer(durationMillis.toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    updateUIWithRemainingTime(millisUntilFinished)
                }

                override fun onFinish() {
                    // Nascondi la textView del tempo rimanente
                    endTimeTextView.visibility = View.INVISIBLE
                    // Resetta endTimeMillis
                    endTimeMillis = 0
                }
            }.start()
        }
    }
    private fun updateUIWithRemainingTime(millisUntilFinished: Long) {
        if (endTimeMillis > 0) {
            val remainingTime = formatTime(millisUntilFinished)
            endTimeTextView.text = "Tempo rimanente: $remainingTime"
            endTimeTextView.visibility = View.VISIBLE
        }
    }

    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hours))
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes))
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
}
