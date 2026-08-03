package com.example.timesheetapp

import android.os.Bundle
import android.widget.TextClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 64.dp)
        ) {
            Row( modifier = Modifier.weight(1f)) {
                DisplayTextClock()
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                AddressSearch()
            }
        }
        Row (
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(64.dp)
        ) {
            Button(
                onClick = {},
                modifier = Modifier.weight(1f).padding(end = 2.dp).fillMaxHeight(),
                shape = RectangleShape
                ) {
                Text("Now")
            }
            Button(
                onClick = {},
                modifier = Modifier.weight(1f).padding(start = 2.dp).fillMaxHeight(),
                shape = RectangleShape) {
                Text("History")
            }
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
fun AddressSearch() {
    val viewModel: AddressViewModel = viewModel()
    val results by viewModel.searchResults.collectAsState()

    var text by remember { mutableStateOf("") }
    var debounceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    Column {
        TextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                debounceJob?.cancel()
                debounceJob = viewModel.viewModelScope.launch {
                    // we have to keep this a 1 sec since that's what Nominatim's usage policy is
                    delay(1000)
                    if (newText.length > 3) { // i didn't want to start a query after just typing one letter so it's just an arbitrary 3 characters
                        viewModel.searchAddress(newText)
                    }
                }
            },
            placeholder = { Text("Enter address") }
        )

        LazyColumn {
            items(results) { place ->
                Text(
                    text = place.display_name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val latitude = place.lat.toDouble()
                            val longitude = place.lon.toDouble()
                            // I was trying to get this to print to the console when clicked, but it's not working
                            println("$latitude, $longitude")
                        }
                        .padding(16.dp)
                    )
            }
        }
    }
}
