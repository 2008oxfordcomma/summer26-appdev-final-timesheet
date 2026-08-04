package com.example.timesheetapp

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.LocalTime

class NowViewModel(app: Application): AndroidViewModel(app) {

    var isClockedIn by mutableStateOf(false)
        private set
    var clockInTime by mutableStateOf<LocalTime?>(null)
        private set
    var location by mutableStateOf(Pair(0.0, 0.0))
        private set
    private val historyViewModel = HistoryLogViewModel(app)
    private val isClockInF: File = File(app.filesDir,"isClockedIn.bin")
    private val clockInF: File = File(app.filesDir,"clockedIn.bin")
    private val locationF: File = File(app.filesDir,"locationF.bin")


    init{
        val isClockData = pullData(isClockInF)
        val clockInData = pullData(clockInF)
        val locationData = pullData((locationF))
        if(isClockData !is Unit){
            isClockedIn = isClockData as Boolean
        }
        if(clockInData !is Unit){
            clockInTime = clockInData as? LocalTime
        }
        if(locationData !is Unit){
            location = pullData(locationF) as Pair<Double, Double>
        }
    }


    fun pullData(file: File): Any{
        if(file.exists()){
            val fileIn: FileInputStream
            val objStreamIn: ObjectInputStream
            try{
                fileIn = FileInputStream(file)
                objStreamIn = ObjectInputStream(fileIn)
                return objStreamIn.readObject()
            } catch(EOF: java.io.EOFException){
                Log.e(this.toString(),  "Error in loading file: ${file.name}")
            }
        } else {
            file.createNewFile()
            Log.d(this.toString(), "Creating in new file: ${file.name}")
        }

        return Unit
    }

    fun updateLocationStatus(isNear: Boolean) {
        if (isNear && !isClockedIn) clockIn()
        else if (!isNear && isClockedIn) clockOut()
    }

    private fun clockIn() {
        isClockedIn = true
        clockInTime = LocalTime.now()
    }

    private fun clockOut() {
        isClockedIn = false
        val clocked = clockInTime ?: throw Error("TRIED CLOCKING OUT WHILE NEVER CLOCKED IN!!!")
        historyViewModel.add(clocked)
    }

    fun getHistory(): List<Work> {
        return historyViewModel.getLog()
    }
}