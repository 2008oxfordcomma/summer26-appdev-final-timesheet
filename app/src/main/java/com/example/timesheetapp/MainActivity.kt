package com.example.timesheetapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timesheetapp.data.viewmodel.AddressViewModel
import com.example.timesheetapp.ui.theme.TimeSheetAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.lifecycle.ViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            TimeSheetAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // amalgamation of Reso Coder, Dr. Parag Shukla, and Kotlin with Compose youtube videos
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onHistory: () -> Unit = {},
    onNow: () -> Unit = {},
    onSetLocation: () -> Unit = {},
    workLocation: String = ""
) {

    var showHistory by remember {mutableStateOf(false)}
    Box(
        modifier = modifier.fillMaxSize()
    ){
        if (showHistory) {
            HistoryScreen(modifier = modifier)
        } else {
            NowScreen(
                modifier = modifier,
                onSetLocation = onSetLocation
            )
        }

        Row (
            modifier = modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.15f)
        ) {
            Button(
                onClick = { showHistory = false },
                modifier = modifier
                    .weight(1f)
                    .padding(end = 2.dp)
                    .fillMaxHeight(),
                shape = RectangleShape
            ) {
                Text("Now")
            }
            Button(
                onClick = { showHistory = true },
                modifier = modifier
                    .weight(1f)
                    .padding(start = 2.dp)
                    .fillMaxHeight(),
                shape = RectangleShape) {
                Text("History")
            }
        }
    }
}

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    histVM: HistoryLogViewModel = viewModel()
) {
    LazyColumn(modifier = modifier
        .fillMaxSize()
        .background(color = Color.Cyan)
    ) {
        item{
            HistoryCard()
        }
        val histData = histVM.getLog()
        for(work in histData){
            item{
                HistoryCard(
                    modifier = modifier,
                    dateText = work.date.toString(),
                    clockInText = (work.clockIn.hour.toString() + ":" + work.clockIn.minute.toString()),
                    clockOutText = (work.clockOut.hour.toString() + ":" + work.clockOut.minute.toString()),
                    totalText = work.total.toString(),
                    delTxt = "x",
                    isHeader = false,
                    onDel = { histVM.remove(work) }
                )
            }
        }
    }
}

@Composable
fun HistoryCard(
    modifier: Modifier = Modifier,
    dateText: String = "Date",
    clockInText:String = "In",
    clockOutText:String = "Out",
    totalText:String = "Total",
    delTxt:String = "",
    isHeader: Boolean = true,
    onDel: () -> Unit = {}
){
    Row(modifier = modifier
        .height(60.dp)
        .background(color = Color.Yellow)){
        val fontSize = 4.em
        Row(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .background(color = Color.Red)
        ){
            Column(modifier = modifier
                .weight(1.0f)
                .fillMaxHeight()) {
                Text(modifier = modifier, text = dateText, fontSize = fontSize)
            }
            Column(modifier = modifier
                .weight(1.0f)
                .fillMaxHeight()) {
                Text(modifier = modifier, text = clockInText, fontSize = fontSize)
            }
            Column(modifier = modifier
                .weight(1.0f)
                .fillMaxHeight()) {
                Text(modifier = modifier, text = clockOutText, fontSize = fontSize)
            }
            Column(modifier = modifier
                .weight(1.0f)
                .fillMaxHeight()) {
                Text(modifier = modifier, text = totalText, fontSize = fontSize)
            }
        }
        Button(modifier = modifier, onClick = onDel, enabled = !isHeader){
            Text(modifier = modifier, text =  delTxt, fontSize = fontSize)
        }
    }
}

@Composable
fun NowScreen(
    modifier: Modifier = Modifier,
    onSetLocation: () -> Unit
){
    val nowViewModel: NowViewModel = viewModel()
//    val isClockedIn by nowViewModel.isClockedIn.collectAsState()
//    val clockInTime by nowViewModel.clockInTime.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 64.dp)
    ) {
        Row( modifier = modifier.weight(1f)) {
            DisplayTextClock()
        }
        Row( modifier = modifier.weight(1f)) {
            ClockInStatus(
                modifier = modifier,
                isWorking = nowViewModel.isClockedIn,
                startTime = if(nowViewModel.isClockedIn) "Clocked in at: ${nowViewModel.clockInTime!!.hour}:${nowViewModel.clockInTime!!.minute}" else "Go to work to clock in"
            )
        }
        Row(
            modifier = modifier.weight(1.5f),
            horizontalArrangement = Arrangement.Center
        ) {
            AddressSearch(modifier = modifier, R.drawable.search)
        }
    }
}

@Composable
fun DisplayTextClock() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AndroidView(
            factory = { context ->
                TextClock(context).apply {
                    format12Hour = "hh:mm:ss a"
                    timeZone = timeZone
                    textSize = 64f
                }
            },
            modifier = Modifier.padding(5.dp),
        )
    }
}

@Composable
fun ClockInStatus(
    modifier: Modifier = Modifier,
    isWorking: Boolean = false,
    startTime: String = "Not clocked in"
){
    Column(modifier = modifier) {
        var statusMessage: String
        val bgColor: Color
        val statusText: String

        if (isWorking) {
            statusMessage = "Clocked In"
            bgColor = Color.Green
            statusText = "You are at work"
        } else {
            statusMessage = "Clocked Out"
            bgColor = Color.Red
            statusText = "You are not at work"
        }
//        Tells you the clockin time
        Column(
            modifier = modifier
                .background(bgColor)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = statusMessage,
                color = Color.Black,
                fontSize = 9.em,
                textAlign = TextAlign.Center
            )
            Text(
                text = startTime,
                color = Color.Black,
                fontSize = 3.em,
                textAlign = TextAlign.Center
            )
            Text(
                text = statusText,
                color = Color.Black,
                fontSize = 4.em,
                textAlign = TextAlign.Center
            )
        }
//        Row(modifier = modifier){
//            Text(
//                modifier = modifier.padding(10.dp),
//                text = "Status:",
//                fontSize = 5.em
//            )
//
//            Text(
//                modifier = modifier.background(bgColor).fillMaxWidth(),
//                text = statusMessage,
//                color = Color.Black,
//                fontSize = 9.em,
//                textAlign = TextAlign.Center
//            )
//        }
    }
}

@Composable
fun AddressSearch(
    modifier: Modifier = Modifier,
    @DrawableRes leadingIcon: Int,
) {
    val viewModel: AddressViewModel = viewModel()
    val nowViewModel: NowViewModel = viewModel()
    val context = LocalContext.current
    val results by viewModel.searchResults.collectAsState()
    val isNearLocation by viewModel.isNearLocation.collectAsState()

    LaunchedEffect(isNearLocation) {
        nowViewModel.updateLocationStatus(isNearLocation)
    }

    var text by remember { mutableStateOf("") }
    var debounceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    var selectedLocation by remember {mutableStateOf<Pair<String, String>?>(null)}
    var showDropdown by remember {mutableStateOf(false)}

    Column (modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        selectedLocation?.let { (lat, lon) ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)) {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selected Location:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Latitude: $lat",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Longitude: $lon",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    viewModel.updateWorkLocation()
                    viewModel.clearResults()
                },
                modifier = Modifier
                    .height(56.dp)
                    .padding(top = 0.dp)
            ) {
                // i need to get an image put into the button
                Text("Current")
            }
            TextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                    showDropdown = newText.length > 3
                    debounceJob?.cancel()
                    debounceJob = viewModel.viewModelScope.launch {
                        // we have to keep this a 1 sec since that's what Nominatim's usage policy is
                        delay(1000.milliseconds)
                        if (newText.length > 3) { // i didn't want to start a query after just typing one letter so it's just an arbitrary 3 characters
                            viewModel.searchAddress(newText)
                        }
                    }
                },
                placeholder = { Text("Enter address") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(modifier = Modifier.size(16.dp), painter = painterResource(id = leadingIcon), contentDescription = null) },

                )
            OutlinedButton(
                onClick = {
                    viewModel.clearSelectedLocation()
                    text = ""
                },
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .padding(top = 0.dp),
            ) {
                Text("X", textAlign = TextAlign.Center)
            }
        }

        if (showDropdown && results.isNotEmpty()) {
            Card(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)) {
                LazyColumn {
                    items(results) { place ->
                        Text(
                            text = place.display_name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    text = place.display_name

                                    val latitude = place.lat.toDouble()
                                    val longitude = place.lon.toDouble()
                                    selectedLocation = Pair(
                                        String.format(Locale.US, "%.6f", latitude),
                                        String.format(Locale.US, "%.6f", longitude)
                                    )
                                    viewModel.setSelectedLocation(latitude, longitude)
                                    // I was trying to get this to print to the console when clicked, but it's not working
                                    println("$latitude, $longitude")
                                    showDropdown = false
                                    viewModel.clearResults()
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
        if (showDropdown && results.isEmpty() && text.length > 3) {
            Text(
                text = "No results found",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}