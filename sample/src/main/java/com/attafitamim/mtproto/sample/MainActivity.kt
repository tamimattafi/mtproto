package com.attafitamim.mtproto.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.attafitamim.mtproto.core.objects.MTMethod
import com.attafitamim.mtproto.core.objects.MTObject
import com.attafitamim.mtproto.core.stream.MTInputStream
import com.attafitamim.mtproto.core.stream.MTOutputStream

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
