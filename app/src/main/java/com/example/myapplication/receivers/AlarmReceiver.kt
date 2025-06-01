package com.example.myapplication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.data.network.mqtt.MqttHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var mqttHandler: MqttHandler
    override fun onReceive(context: Context, intent: Intent) {
        val topic = intent.getStringExtra("topic") ?: return
        mqttHandler.publishMessage("1", topic)
    }
}