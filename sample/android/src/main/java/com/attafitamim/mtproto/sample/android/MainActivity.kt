package com.attafitamim.mtproto.sample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.attafitamim.mtproto.client.api.bodies.RequestError
import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.api.connection.IRequestDelegate
import com.attafitamim.mtproto.client.sockets.connection.SocketConnectionManager
import com.attafitamim.mtproto.sample.android.ui.theme.MTProtoSampleTheme
import com.attafitamim.scheme.mtproto.methods.global.TLPing
import com.attafitamim.scheme.mtproto.types.global.TLPong
import java.lang.Exception
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    lateinit var connectionManager: IConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MTProtoSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        connectionManager = ConnectionHelper.createConnectionManager()

        val request = TLPing(Random.nextLong())

        val requestDelegate = object : IRequestDelegate<TLPong> {
            override fun onError(error: RequestError) {
                TODO("Not yet implemented")
            }

            override fun onException(exception: Exception) {
                TODO("Not yet implemented")
            }

            override fun onResponse(response: TLPong) {
                TODO("Not yet implemented")
            }

        }

        connectionManager.sendRequest(request, requestDelegate, null, 0)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MTProtoSampleTheme {
        Greeting("Android")
    }
}