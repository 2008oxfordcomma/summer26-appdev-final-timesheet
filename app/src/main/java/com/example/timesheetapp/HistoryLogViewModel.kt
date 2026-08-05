package com.example.timesheetapp

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import java.time.LocalDate
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.LocalTime


class HistoryLogViewModel(app: Application): AndroidViewModel(app) {

    private val file: File = File(app.filesDir,"log.bin")
    //    TODO fix this by not reassigning it
    var historyLog by mutableStateOf<MutableList<Work>>(mutableListOf<Work>())

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

    fun getLog(): SnapshotStateList<Work> {
        val backLogDate = LocalDate.now().minusDays(14)
        val returnList = mutableStateListOf<Work>()
        var indx: Int = historyLog.size-1
        while(indx > 0 && (backLogDate.isBefore(historyLog[indx].date) || backLogDate.isEqual(historyLog[indx].date))){
            returnList.add(historyLog[indx])
            indx--
        }

        return returnList
    }

}