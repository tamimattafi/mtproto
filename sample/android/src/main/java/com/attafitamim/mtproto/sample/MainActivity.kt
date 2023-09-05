package com.attafitamim.mtproto.sample

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.attafitamim.mtproto.client.android.core.AndroidUtilities
import com.attafitamim.mtproto.client.android.core.NativeLoader
import com.attafitamim.mtproto.client.android.core.SharedConfig
import com.attafitamim.mtproto.client.android.tgnet.BackendInfo
import com.attafitamim.mtproto.client.android.tgnet.ConnectionsManager
import com.attafitamim.mtproto.client.api.bodies.RequestError
import com.attafitamim.mtproto.client.api.connection.ConnectionFlags
import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.api.connection.IRequestDelegate
import com.attafitamim.mtproto.client.api.handlers.IUpdateHandler
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject
import com.attafitamim.mtproto.sample.ui.theme.MTProtoSampleTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var connectionManager: IConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionManager = initConnectionManager()

        setContent {
            MTProtoSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Greeting(name = "Hello world")
                        SendRequest {
                            sendRequest()
                        }
                    }
                }
            }
        }
    }

    private fun initConnectionManager(): ConnectionsManager {
        AndroidUtilities.init(this)
        SharedConfig.init(this)
        NativeLoader.initNativeLibs(applicationContext)

        val deviceId = "test_device_123"

        val backendInfo = BackendInfo(
            ipAddress = "",
            port = 2045,
            publicKey = ""
        )

        val appId = 123
        val layer = 128

        val updateParser = IUpdateHandler { inputStream ->
            // Parse update
            TODO("IUpdateHandler not implemented")
        }

        ConnectionsManager.native_setJava(false)
        return ConnectionsManager(
            0,
            backendInfo,
            getFilesDirFixed(),
            deviceId,
            applicationContext,
            123,
            "123",
            layer,
            appId,
            "",
            false,
            updateParser,
            null
        )
    }

    fun getFilesDirFixed(): File? {
        for (a in 0..9) {
            return applicationContext.filesDir
        }

        try {
            val info: ApplicationInfo = applicationContext.applicationInfo
            val path = File(info.dataDir, "files")
            path.mkdirs()
            return path
        } catch (e: Exception) {
        }

        return File("/data/data/com.attafitamim.mtproto.sample/files")
    }

    private fun sendRequest() {
        val method = object : TLMethod<TLObject> {
            override val constructorHash: Int = 123
            override fun parse(inputStream: TLInputStream): TLObject = object : TLObject {
                override val constructorHash: Int = 456
                override fun serialize(outputStream: TLOutputStream) {
                    outputStream.writeInt(constructorHash)
                }
            }

            override fun serialize(outputStream: TLOutputStream) {
                outputStream.writeInt(constructorHash)
            }
        }

        val requestDelegate = object : IRequestDelegate<TLObject> {
            override fun onError(error: RequestError) {
                Log.e("ConnectionManager","ConnectionManager.onError: $error")
                Toast.makeText(
                    this@MainActivity,
                    "ConnectionManager.onError: $error",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onException(exception: java.lang.Exception) {
                Log.e("ConnectionManager","ConnectionManager.onException: $exception")
                Toast.makeText(
                    this@MainActivity,
                    "ConnectionManager.onException: $exception",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onResponse(response: TLObject) {
                Log.e("ConnectionManager","ConnectionManager.onResponse: $response")
                Toast.makeText(
                    this@MainActivity,
                    "ConnectionManager.onResponse: $response",
                    Toast.LENGTH_LONG
                ).show()
            }

        }

        connectionManager.sendRequest(
            method,
            requestDelegate,
            ConnectionFlags.RequestFlagEnableUnauthorized,
            ConnectionFlags.ConnectionTypeGeneric
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun SendRequest(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = "Send Request",
            modifier = textModifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MTProtoSampleTheme {
        Greeting("Android")
    }
}