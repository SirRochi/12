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
import android.util.Log
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
    private val LOCATION_PERMISSION_REQUEST_CODE = 0
    private var endTimeMillis: Long = 0
    private lateinit var endTimeTextView: TextView
    private var selectedHourOfDay: Int = 0
    private var selectedMinute: Int = 0



// metodo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inizializzazione dei bottoni
        endTimeTextView = findViewById(R.id.timeRemainingTextView)

        parkButton = findViewById(R.id.parkButton)
        exitButton = findViewById(R.id.exitButton)

        // Bottone per impostare la durata
        val durationButton: Button = findViewById(R.id.setDurationButton)


        durationButton.setOnClickListener {
            showTimePickerDialog()

        }
// ascoltatori
        // Bottone per uscire dall'applicazione
        exitButton.setOnClickListener {
            finish()
        }

        val showMapButton: Button = findViewById(R.id.showMapButton)
        showMapButton.setOnClickListener {
            showLocationOnMap()
        }



        // barra di siste,a
        configureSystemBarsAppearance()

        // Inizializza le altre variabili
        initializeViews()
        initializeLocationManager()




        // Corretta posizione di PARK BOTTON
        parkButton.setOnClickListener {
            // Controlla se è stata concessa l'autorizzazione alla posizione
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Ottieni l'ultima posizione conosciuta
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (lastKnownLocation != null) {
                    // Salva la posizione senza aprire Google Maps
                    saveLocationWithoutOpeningMap(lastKnownLocation.latitude, lastKnownLocation.longitude)

                    // Ferma il timer attuale, se presente
                    timer?.cancel()

                    // Avvia il timer con una durata predefinita
                    Log.d("MainActivity", "Before startTimer call")
                    startTimer(selectedHourOfDay * 60 * 60 * 1000 + selectedMinute * 60 * 1000)
                    Log.d("MainActivity", "After startTimer call")

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
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE )           }
        }

    }
    private fun saveLocationWithoutOpeningMap(latitude: Double, longitude: Double) {
        // Implementa la logica per salvare la posizione della macchina senza aprire nulla
        // Puoi memorizzare la latitudine e la longitudine in una variabile o persistere secondo necessità
        parkedLocation = Location("PosizioneParcheggio")
        parkedLocation!!.latitude = latitude
        parkedLocation!!.longitude = longitude


    }


    private fun showLocationOnMap() {
        Log.d("MainActivity", "showLocationOnMap() called")

        if (parkedLocation != null) {
            val latitude = parkedLocation!!.latitude
            val longitude = parkedLocation!!.longitude
            val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Parking Location)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // se Google Maps non è installato sul dispositivo
                Toast.makeText(
                    this,
                    "Google Maps is not installed on your device. Install Google Maps to use this feature.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            //  se la posizione non è salvata
            Toast.makeText(
                this,
                "Location not saved. Please park your car first.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



//barra di sistema
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
            Log.d("LocationListener", "Nuova posizione: $latitude, $longitude")
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

                // Ferma il timer
                timer?.cancel()

               // Imposta la nuova durata
                endTimeMillis = selectedDuration.toLong()
                // Aggiorna il testo del pulsante con l'orario selezionato
                val buttonText = String.format(Locale.getDefault(), "Duration: %02d:%02d", hourOfDay, minute)
                setDurationButton.text = buttonText

                // Aggiorna endTimeTextView con l'orario selezionato
                val endTimeText = String.format(Locale.getDefault(), "Remaining time: %02d:%02d", hourOfDay, minute)
                endTimeTextView.text = endTimeText
                endTimeTextView.visibility = View.VISIBLE

                // selziona ora
                selectedHourOfDay = hourOfDay
                selectedMinute = minute
            },
            currentHour,
            currentMinute,
            is24HourFormat
        )

        // Imposta il titolo per i minuti
        timePickerDialog.setTitle("Select Time")

        timePickerDialog.show()
    }


    private fun showDialogWithExplanation() {

    }
    // Avvia un timer
    private fun startTimer(durationMillis: Int) {
        Log.d("MainActivity", "startTimer: Duration = $durationMillis")
        endTimeMillis = System.currentTimeMillis() + durationMillis

        timer?.cancel()  // Interrompe il timer attuale se è in esecuzione

        timer = object : CountDownTimer(durationMillis.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateUIWithRemainingTime(millisUntilFinished)
            }

            override fun onFinish() {
                // Nascondi la textView del tempo rimanente
                endTimeTextView.visibility = View.INVISIBLE
                // Resetta endTimeMillis
                endTimeMillis = 0
                timer = null
                Log.d("MainActivity", "Timer onFinish")
            }
        }.start()

        Log.d("MainActivity", "Timer started")
    }

    // Aggiorna l'interfaccia utente con il tempo rimanente durante il conteggio alla rovescia
    private fun updateUIWithRemainingTime(millisUntilFinished: Long) {
        // Verifica se endTimeMillis è maggiore di zero, assicurandosi che il timer sia attivo
        if (endTimeMillis > 0) {
            // Formatta il tempo rimanente nel formato HH:mm:ss utilizzando la funzione formatTime
            val remainingTime = formatTime(millisUntilFinished)
            endTimeTextView.text = "Remaining time: $remainingTime"
            endTimeTextView.visibility = View.VISIBLE
        }
    }
    // Converte il tempo in millisecondi nel formato HH:mm:ss
    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hours))
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes))
        // Restituisce una stringa formattata con due cifre per ore, minuti e secondi
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

}
