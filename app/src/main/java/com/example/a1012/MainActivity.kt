package com.example.a1012

// MainActivity.kt


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


class MainActivity : ComponentActivity() {

    private lateinit var parkButton: Button
    private lateinit var exitButton: Button
    private lateinit var locationManager: LocationManager
    private var parkedLocation: Location? = null
    private var timer: CountDownTimer? = null
    private val durationMillis: Long = 30 * 60 * 1000 // 30 minuti
    private val LOCATION_PERMISSION_REQUEST_CODE = 0
    private val mainScope = MainScope()
    private var endTimeMillis: Long = 0
    private lateinit var endTimeTextView: TextView // Dichiarazione di endTimeTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inizializzazione di endTimeTextView
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

        // Inizializza le altre variabili
        parkButton = findViewById(R.id.parkButton)
        exitButton = findViewById(R.id.exitButton)

        // Inizializza il LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Richiedi i permessi di ACCESS_FINE_LOCATION, assicurati di avere il permesso nel manifest
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Aggiungi il listener per ottenere gli aggiornamenti sulla posizione
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } else {
            // Richiedi i permessi se non sono già concessi
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Spiega all'utente perché stai richiedendo il permesso
                // Puoi utilizzare un AlertDialog o un'altra forma di messaggio
                // per fornire una spiegazione più descrittiva.
                showDialogWithExplanation()
            } else {
                // Richiedi i permessi
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
            // Aggiorna la posizione corrente dell'utente
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

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // Calcola la durata in millisecondi
                val selectedDuration = (hourOfDay * 60 + minute) * 60 * 1000
                startTimer(selectedDuration)
            },
            currentHour,
            currentMinute,
            true
        )

        timePickerDialog.show()
    }


    private fun showDialogWithExplanation() {
        // Implementa il codice per mostrare una spiegazione al richiedere i permessi
    }

    private fun startTimer(durationMillis: Int) {
        if (timer == null) {
            endTimeMillis = System.currentTimeMillis() + durationMillis

            timer = object : CountDownTimer(durationMillis.toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // Handle the ticking logic if needed
                }

                override fun onFinish() {
                    // Handle the timer finish logic if needed
                }
            }.start()
        }
    }
}