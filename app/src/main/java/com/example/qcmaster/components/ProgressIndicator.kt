package com.ui.components



import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LinearDeterminateIndicator() {
    var currentProgress by remember { mutableStateOf(0f) }
    var loading by remember { mutableStateOf(true) } // ✨ Start loading immediately
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            loadProgress { progress -> currentProgress = progress }
            loading = false
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (loading) {
            Text(text = "Loading classes...") // ✨ Text while loading
            LinearProgressIndicator(
                progress = currentProgress,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
           // Text(text = "Loading completed ✅")
        }
    }
}

suspend fun loadProgress(updateProgress: (Float) -> Unit) {
    for (i in 1..100) {
        updateProgress(i.toFloat() / 100)
        delay(30)
    }
}