package com.example.timesheetapp.data.viewmodel

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.example.timesheetapp.data.model.AddressResult
import com.example.timesheetapp.data.remote.NominatimClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.math.abs

class AddressViewModel(application: android.app.Application) : AndroidViewModel(application) {

    private val _searchResults = MutableStateFlow<List<AddressResult>>(emptyList())
    val searchResults: StateFlow<List<AddressResult>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isNearLocation = MutableStateFlow(false)
    val isNearLocation: StateFlow<Boolean> = _isNearLocation

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    var workLocation by mutableStateOf(Pair(0.0, 0.0))
    var selectedLocation by mutableStateOf<Pair<String, String>?>(null)
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var isPolling = false
    private val workLocationF: File = File(application.filesDir,"locationF.bin")

    private val context: Context = application

    init{
        if(workLocationF.exists()){
            val fileIn: FileInputStream
            val objStreamIn: ObjectInputStream
            try{
                fileIn = FileInputStream(workLocationF)
                objStreamIn = ObjectInputStream(fileIn)
                workLocation = objStreamIn.readObject() as Pair<Double, Double>
                fileIn.close()
                objStreamIn.close()

            } catch(EOF: java.io.EOFException){
                Log.e(this.toString(),  "Error in loading file: ${workLocationF.name}\n${EOF.toString()}")
            }
        } else {
            workLocationF.createNewFile()
            Log.d(this.toString(), "Creating in new file: ${workLocationF.name}")
        }

        Log.d(this.toString(), "Opening File ${workLocationF.name}. Saved Contents: ${workLocation.first},${workLocation.second}")
    }

    fun searchAddress(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val results = NominatimClient.api.searchAddress(query)
                _searchResults.value = results
            } catch (e: IOException) {
                _error.value = "Network error: ${e.message}"
            } catch (e: HttpException) {
                _error.value = "Server error: ${e.message()}"
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateWorkLocation(){
        val fileOut: FileOutputStream = FileOutputStream(workLocationF)
        val objStreamOut: ObjectOutputStream = ObjectOutputStream(fileOut)
        getCurrentLocation()
        objStreamOut.writeObject(workLocation)

        fileOut.close()
        objStreamOut.close()
        Log.d(this.toString(), "Saving Work Location, Lat: ${workLocation.first} Long: ${workLocation.second}")
    }
    fun clearResults() {
        _searchResults.value = emptyList()
        _error.value = null
    }

    fun setSelectedLocation(latitude: Double, longitude: Double) {
        selectedLatitude = latitude
        selectedLongitude = longitude
        _isNearLocation.value = false
        startLocationPolling()
    }

    fun clearSelectedLocation() {
        selectedLatitude = null
        selectedLongitude = null
        _isNearLocation.value = false
        isPolling = false
    }

    // for future reference https://www.youtube.com/watch?v=qZBKoBew010
    private fun startLocationPolling() {
        if (isPolling) return
        isPolling = true

        viewModelScope.launch {
            while (isPolling) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    getCurrentLocation { latitude, longitude ->
                        _currentLocation.value = Pair(latitude, longitude)
                        checkIfNearLocation(latitude, longitude)
                    }
                }
                delay(60000) // 1 minute
            }
        }
    }

    private fun checkIfNearLocation(currentLatitude: Double, currentLongitude: Double) {
        val targetLatitude = selectedLatitude ?: return
        val targetLongitude = selectedLongitude ?: return

        val threshold = 0.005
        val latitudeDifference = abs(currentLatitude - targetLatitude)
        val longitudeDifference = abs(currentLongitude - targetLongitude)

        val isNear = latitudeDifference < threshold && longitudeDifference < threshold
        _isNearLocation.value = isNear
    }

    fun stopPolling() {
        isPolling = false
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }

    private fun getCurrentLocation(){
        getCurrentLocation(onLocationResult = {a: Double, b: Double -> {}})
    }

    // amalgamation of Reso Coder, Dr. Parag Shukla, and Kotlin with Compose youtube videos
    fun getCurrentLocation(onLocationResult: (Double, Double) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("AddressViewModel", "Location permission not granted")
            _error.value = "Location permission needed"
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                this.workLocation = Pair(latitude, longitude)
                Log.d("AddressViewModel", "Location: $latitude, $longitude")
                onLocationResult(latitude, longitude)
            } else {
                Log.e("AddressViewModel", "Location is null")
                _error.value = "Could not get location"
            }
        }.addOnFailureListener { e ->
            Log.e("AddressViewModel", "Error getting location", e)
            _error.value = "Error getting location: ${e.message}"
        }
    }
}

