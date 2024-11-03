package com.example.myapplication.mqtt

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.Exception

class MqttHandler private constructor(private val context: Context) {

    private val mqttClient: MqttAndroidClient by lazy {
        MqttAndroidClient(context, getBrokerUri(), MqttClient.generateClientId())
    }

    init {
        connect()
    }

    private fun connect() {
        val options = MqttConnectOptions().apply {
            userName = getUsername()
            password = getKey().toCharArray()
        }

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.i("MQTT", "Connection successful")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "Exception during connection", exception)
            }
        })
    }

    private fun getUsername(): String {
        val sp = context.getSharedPreferences("custom_prefs", 0)
        return sp.getString("username_key", "") ?: ""
    }

    private fun getKey(): String {
        val sp = context.getSharedPreferences("custom_prefs", 0)
        return sp.getString("key_key", "") ?: ""
    }

    private fun getBrokerUri(): String {
        val sp = context.getSharedPreferences("custom_prefs", 0)
        return sp.getString("host_key", "") ?: ""
    }

    fun publishMessage(message: String, topic: String) {
        if (mqttClient.isConnected) {
            try {
                val mqttMessage = MqttMessage().apply {
                    payload = message.toByteArray()
                }
                mqttClient.publish(topic, mqttMessage)
                Log.i("MQTT", "Message published successfully")
            } catch (e: Exception) {
                Log.e("MQTT", "Exception during publish: ", e)
            }
        } else {
            Log.e("MQTT", "Client is not connected")
        }
    }

    fun setCallback(callback: MqttCallback) {
        mqttClient.setCallback(callback)
    }

    companion object {
        @Volatile
        private var INSTANCE: MqttHandler? = null

        fun getInstance(context: Context): MqttHandler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MqttHandler(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
