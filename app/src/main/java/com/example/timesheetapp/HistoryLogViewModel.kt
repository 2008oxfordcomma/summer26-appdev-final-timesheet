package com.example.timesheetapp

import android.app.Application
import android.util.JsonWriter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.serialization.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


class HistoryLogViewModel(app: Application): AndroidViewModel(app) {

    private val file: File = File(app.filesDir,"log.bin")
    private var historyLog by mutableStateOf<MutableList<Work>>(mutableListOf<Work>())

    init{
        if(file.exists()){
            val fileIn: FileInputStream
            val objStreamIn: ObjectInputStream
            try{
                fileIn = FileInputStream(file)
                objStreamIn = ObjectInputStream(fileIn)
                historyLog = objStreamIn.readObject() as MutableList<Work>
                objStreamIn.close()
                fileIn.close()
            } catch(EOF: java.io.EOFException){
                println("No history data has been saved")
            }

            println(historyLog)
        } else {
            file.createNewFile()
            println("creating new history file")
        }
    }


    fun add(clockIn: LocalTime, newWork: Work = Work(clockIn = clockIn)): Unit{
        historyLog.add(newWork)
        saveLog()
    }

    fun remove(work: Work){
        historyLog.remove(work)
        saveLog()
    }

    fun saveLog(){
        val fileOut: FileOutputStream = FileOutputStream(file)
        val objStreamOut: ObjectOutputStream = ObjectOutputStream(fileOut)
        objStreamOut.writeObject(historyLog)

        fileOut.close()
        objStreamOut.close()
        println(historyLog)
    }

    fun getLog(): List<Work> {
        val backLogDate = LocalDate.now().minusDays(14)
        val returnList = mutableListOf<Work>()
        var indx: Int = historyLog.size-1
        while(indx > 0 && (backLogDate.isBefore(historyLog[indx].date) || backLogDate.isEqual(historyLog[indx].date))){
            returnList.add(historyLog[indx])
            indx--
        }

        return returnList.toList()
    }

}