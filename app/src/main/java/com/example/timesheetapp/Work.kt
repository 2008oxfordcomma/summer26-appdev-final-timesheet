package com.example.timesheetapp

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime

data class Work(
    val date: LocalDate = LocalDate.now(),
    val clockIn: LocalTime,
    val clockOut: LocalTime = LocalTime.now(),
    val total: Double = ((clockOut.hour - (clockIn.hour)) * 60 + (clockOut.minute - clockIn.minute))/60.0
): Serializable