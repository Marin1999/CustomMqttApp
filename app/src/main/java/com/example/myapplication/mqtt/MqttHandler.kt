package com.example.myapplication.mqtt

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.Exception
import javax.net.ssl.SSLSocketFactory

class MqttHandler private constructor(private val context: Context) {

    private val mqttClient: MqttAndroidClient by lazy {
        MqttAndroidClient(context, getBrokerUri(), MqttClient.generateClientId())
    }

    private val sharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    init {
        connect()
    }

    private fun connect() {
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            socketFactory = SSLSocketFactory.getDefault()
        }
        val useAuthentication = sharedPreferences.getString("checkbox_key", "false")?.toBoolean()?: false
        if(useAuthentication){
            options.userName = getUsername()
            options.password = getKey().toCharArray()
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
        return sharedPreferences.getString("username_key", "") ?: ""
    }

    private fun getKey(): String {
        return sharedPreferences.getString("key_key", "") ?: ""
    }

    private fun getBrokerUri(): String {
        return sharedPreferences.getString("host_key", "") ?: ""
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
