package com.example.myapplication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.di.MqttHandlerEntryPoint
import dagger.hilt.android.EntryPointAccessors

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            MqttHandlerEntryPoint::class.java
        )

        val mqttHandler = entryPoint.mqttHandler()
        val topic = intent.getStringExtra("topic") ?: return
        mqttHandler.publishMessage("1", topic)
    }

}