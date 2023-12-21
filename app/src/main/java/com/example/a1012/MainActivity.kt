package com.example.a1012

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
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

        // Inizializzazione di endTimeTextView
        endTimeTextView = findViewById(R.id.timeRemainingTextView)

        parkButton = findViewById(R.id.parkButton)
        exitButton = findViewById(R.id.exitButton)

        val durationButton: Button = findViewById(R.id.setDurationButton)

        durationButton.setOnClickListener {
            showTimePickerDialog()
        }

        // Controlla se il dispositivo esegue Android 11 o versioni successive
        configureSystemBarsAppearance()

        // Inizializza le altre variabili
        initializeViews()
        initializeLocationManager()

        // Corretta posizione di parkButton.setOnClickListener
        parkButton.setOnClickListener {
            // Controlla se è stata concessa l'autorizzazione alla posizione
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Ottieni l'ultima posizione conosciuta
                val lastKnownLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (lastKnownLocation != null) {
                    // Salva la posizione su Google Maps (apre Google Maps con la posizione di parcheggio)
                    saveLocationOnGoogleMaps(lastKnownLocation.latitude, lastKnownLocation.longitude)

                    // Avvia il timer con una durata predefinita (es. 30 minuti)
                    startTimer(durationMillis.toInt())
                } else {
                    // Gestisci il caso in cui l'ultima posizione conosciuta non è disponibile
                    Toast.makeText(
                        this,
                        "Impossibile recuperare la posizione attuale. Riprova più tardi.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Richiedi le autorizzazioni di posizione se non sono già state concesse
                // (potresti voler gestire la spiegazione e la richiesta di autorizzazione come nel tuo codice esistente)
            }
        }
    }

    private fun configureSystemBarsAppearance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            // Per dispositivi precedenti ad Android 11
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun initializeViews() {
        parkButton = findViewById(R.id.parkButton)
        exitButton = findViewById(R.id.exitButton)
    }

    private fun initializeLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Richiedi le autorizzazioni ACCESS_FINE_LOCATION, assicurati di avere l'autorizzazione nel manifesto
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Aggiungi un listener per ottenere gli aggiornamenti sulla posizione
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } else {
            handleLocationPermissionRequest()
        }
    }

    private fun handleLocationPermissionRequest() {
        // Richiedi le autorizzazioni se non sono già state concesse
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            showDialogWithExplanation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Aggiorna la posizione attuale dell'utente
            val latitude = location.latitude
            val longitude = location.longitude
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Gestisci i cambiamenti di stato se necessario
        }

        override fun onProviderEnabled(provider: String) {
            // Chiamato quando l'utente abilita il provider della posizione
        }

        override fun onProviderDisabled(provider: String) {
            // Chiamato quando l'utente disabilita il provider della posizione
        }
    }

    // Bottone della durata
private fun showTimePickerDialog() {
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)

    val setDurationButton: Button = findViewById(R.id.setDurationButton)
    val endTimeTextView: TextView = findViewById(R.id.timeRemainingTextView)
    val formatCheckBox: CheckBox = findViewById(R.id.formatCheckBox)

    val is24HourFormat = formatCheckBox.isChecked

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
        is24HourFormat
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

    private fun saveLocationOnGoogleMaps(latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Parking Location)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Handle the case where Google Maps is not installed on the device
            Toast.makeText(
                this,
                "Google Maps is not installed on your device. Please install Google Maps to use this feature.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
