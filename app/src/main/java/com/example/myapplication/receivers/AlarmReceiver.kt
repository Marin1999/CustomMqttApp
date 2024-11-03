package com.example.myapplication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.mqtt.MqttHandler

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val topic = intent.getStringExtra("topic") ?: return



        val mqttHandler = MqttHandler.getInstance(context)
        mqttHandler.publishMessage("1",topic)
    }

}