package com.example.timesheetapp 

import android.os.Bundle
import android.widget.TextClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
//import androidx.compose.material3.Button
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onHistory: () -> Unit = {},
    onNow: () -> Unit = {},
    onSetLocation: () -> Unit = {},
    workLocation: String = ""
) {
    Box(
        modifier = modifier.fillMaxSize()
    ){
        NowScreen(
            modifier = modifier,
            onSetLocation = onSetLocation
        )
        Row (
            modifier = modifier.align(Alignment.BottomCenter).fillMaxWidth().height(64.dp)
        ) {
            Button(
                onClick = onNow,
                modifier = modifier.weight(1f).padding(end = 2.dp).fillMaxHeight(),
                shape = RectangleShape
                ) {
                Text("Now")
            }
            Button(
                onClick = onHistory,
                modifier = modifier.weight(1f).padding(start = 2.dp).fillMaxHeight(),
                shape = RectangleShape) {
                Text("History")
            }
        }
    }
}

@Composable
fun NowScreen(
    modifier: Modifier = Modifier,
    onSetLocation: () -> Unit
){
    Column(
        modifier = modifier.fillMaxSize().padding(bottom = 64.dp)
    ) {
        Row( modifier = modifier.weight(1f)) {
            DisplayTextClock()
        }
        Row( modifier = modifier.weight(1f)) {
            ClockInStatus(modifier = modifier)
        }
        Row(
            modifier = modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onSetLocation){

            }
            AddressSearch()
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
    startTime: String = "test"
){
    Column(modifier = modifier) {
        var statusMessage: String = ""
        val bgColor: Color

        if (isWorking) {
            statusMessage = "Clocked In"
            bgColor = Color.Green
        } else {
            statusMessage = "Clocked Out"
            bgColor = Color.Red
        }
//        Tells you the clockin time
        Text(
            modifier = modifier.padding(10.dp),
            text = startTime,
            fontSize = 6.em
        )

        Text(
            modifier = modifier.background(bgColor).fillMaxWidth(),
            text = statusMessage,
            color = Color.Black,
            fontSize = 9.em,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddressSearch() {
    val viewModel: AddressViewModel = viewModel()
    val results by viewModel.searchResults.collectAsState()

    var text by remember { mutableStateOf("") }
    var debounceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    var selectedLocation by remember {mutableStateOf<Pair<String, String>?>(null)}
    var showDropdown by remember {mutableStateOf(false)}

    Column (modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        selectedLocation?.let { (lat, lon) ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
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
            modifier = Modifier.fillMaxWidth()
        )

        if (showDropdown && results.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                LazyColumn {
                    items(results) { place ->
                        Text(
                            text = place.display_name,
                            modifier = Modifier.fillMaxWidth().clickable {
                                    text = place.display_name

                                    val latitude = place.lat.toDouble()
                                    val longitude = place.lon.toDouble()
                                    selectedLocation = Pair(
                                        String.format(Locale.US, "%.6f", latitude),
                                        String.format(Locale.US, "%.6f", longitude)
                                    )
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
                color = androidx.compose.ui.graphics.Color.Gray
            )
        }
    }
}
