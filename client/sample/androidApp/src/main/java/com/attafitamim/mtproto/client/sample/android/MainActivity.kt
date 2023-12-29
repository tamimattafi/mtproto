package com.attafitamim.mtproto.client.sample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.attafitamim.mtproto.client.sample.ConnectionHelper
import com.attafitamim.mtproto.client.sample.Greeting
import com.attafitamim.mtproto.client.sample.Playground
import com.russhwolf.settings.SharedPreferencesSettings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val settings by lazy {
        val sharedPreferences = getSharedPreferences(
            "mtproto",
            MODE_PRIVATE
        )

        SharedPreferencesSettings(sharedPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GreetingView(Greeting().greet())
                    Button(
                        modifier = Modifier.height(200.dp).width(200.dp),
                        onClick = ::startConnectionManager
                    ) {
                        Text(text = "Connect")
                    }
                }
            }
        }
    }

    private fun startConnectionManager() = ConnectionHelper.scope.launch {
        Playground.initConnection(settings)
    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("Hello, Android!")
    }
}
