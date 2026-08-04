package com.example.timesheetapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime

class NowViewModel(app: Application): AndroidViewModel(app) {
    //    TODO private member variables
    private val _isClockedIn = MutableStateFlow(false)
    private val _clockInTime = MutableStateFlow<String?>(null)
    private val _clockOutTime = MutableStateFlow<String?>(null)
    private val historyViewModel = HistoryLogViewModel(app)
    private var currentWork: Work? = null

    val isClockedIn: StateFlow<Boolean> = _isClockedIn
    val clockInTime: StateFlow<String?> = _clockInTime
    val clockOutTime: StateFlow<String?> = _clockOutTime



    //    TODO init block that initalize seriablize data to store

    //    TODO functions that change private member variables
    fun updateLocationStatus(isNear: Boolean) {
        if (isNear && !_isClockedIn.value) clockIn()
        else if (!isNear && _isClockedIn.value) clockOut()
    }

    private fun clockIn() {
        val currentTime = LocalTime.now()
        _isClockedIn.value = true
        _clockInTime.value = currentTime.toString()

        currentWork = Work(clockIn = currentTime)
        historyViewModel.add(clockIn = currentTime)
    }

    private fun clockOut() {
        val currentTime = LocalTime.now()
        _isClockedIn.value = false
        _clockOutTime.value = currentTime.toString()

        currentWork?.let { work ->
            val updatedWork = Work(
                date = work.date,
                clockIn = work.clockIn,
                clockOut = currentTime
            )
            historyViewModel.remove(work)
            historyViewModel.add(clockIn = work.clockIn, newWork = updatedWork)
            currentWork = null
        }
    }

    fun getHistory(): List<Work> {
        return historyViewModel.getLog()
    }
}