package com.example.a1012

// MainActivity.kt


import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.a1012.R.*
import kotlinx.coroutines.*
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var parkButton: Button
    private lateinit var showMapButton: Button
    private lateinit var exitButton: Button // Aggiunto il pulsante di uscita
    private lateinit var locationManager: LocationManager
    private var parkedLocation: Location? = null
    private var timer: CountDownTimer? = null
    private val durationMillis: Long = 30 * 60 * 1000 // 30 minuti
    private val LOCATION_PERMISSION_REQUEST_CODE = 0
    private val mainScope = MainScope()
    private lateinit var endTimeTextView: TextView
    private var endTimeMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the device is running Android 11 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            // For devices below Android 11
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_VISIBLE
        }

        timerTextView = findViewById(id.timerTextView)
        startButton = findViewById(id.startButton)
        stopButton = findViewById(id.stopButton)
        parkButton = findViewById(id.parkButton)
        showMapButton = findViewById(id.showMapButton)
        exitButton = findViewById(id.exitButton)
        endTimeTextView = findViewById(id.endTimeTextView)

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


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun requestLocationPermission() {
        // Richiedi i permessi
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }private fun showParkingLocationOnMap() {
        // Assicurati che la posizione del parcheggio sia stata memorizzata
        if (parkedLocation != null) {
            // Costruisci l'URI per aprire Google Maps con la posizione del parcheggio
            val uri = "geo:${parkedLocation!!.latitude},${parkedLocation!!.longitude}?q=${parkedLocation!!.latitude},${parkedLocation!!.longitude}(Posizione del parcheggio)"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.setPackage("com.google.android.apps.maps")

            // Verifica che l'app di Google Maps sia installata sul dispositivo
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                showToast("Google Maps non è installato.")
            }
        } else {
            showToast("Posizione del parcheggio non memorizzata.")
        }
    }
    private fun showDialogWithExplanation() {

    }

    override fun onDestroy() {
        timer?.cancel() // Cancella il timer se è attivo
        lifecycleScope.cancel()
        super.onDestroy()
    }

    private fun startTimer() {
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        endTimeTextView.visibility = View.VISIBLE
        val timeRemainingTextView: TextView = findViewById(R.id.timeRemainingTextView)
        timeRemainingTextView.visibility = View.VISIBLE // Rendi la TextView visibile

        if (timer == null) {
            endTimeMillis = System.currentTimeMillis() + durationMillis // Calcola l'orario di fine sosta

            timer = object : CountDownTimer(durationMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val progress = ((millisUntilFinished.toFloat() / durationMillis) * 100).toInt()

                    // Calcola il tempo rimanente e aggiorna la TextView
                    val remainingTime = formatTime(millisUntilFinished)
                    timeRemainingTextView.text = "Tempo rimanente: $remainingTime"

                    progressBar.progress = progress
                }

                override fun onFinish() {
                    timerTextView.text = "Tempo scaduto!"
                    progressBar.progress = 0
                    progressBar.visibility = View.GONE
                    endTimeTextView.visibility = View.GONE
                    timeRemainingTextView.visibility = View.GONE // Nascondi la TextView quando il timer è scaduto
                }
            }.start()
        }
    }


    // Funzione per formattare il tempo rimanente
    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hours))
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes))
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Listener per ottenere gli aggiornamenti sulla posizione
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
}
