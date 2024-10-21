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

class MqttHandler(private val context: Context) {

    private val mqttClient: MqttAndroidClient by lazy {
        MqttAndroidClient(context,getBrokerUri(),MqttClient.generateClientId())
    }
    init {
        connect()
    }

    private fun connect(){
        val options = MqttConnectOptions()
        options.userName = getUsername()
        options.password = getKey().toCharArray()

        mqttClient.connect(options,null,object : IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.i("MQTT","Connection succesfull")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT","Exception during connection", exception)

            }
        })
    }
    private fun getUsername(): String {
        val sp = context?.getSharedPreferences("custom_prefs", 0)  // Use the same SharedPreferences file
        return sp?.getString("username_key", "") ?: ""  // Fetch using the correct key (username_key)
    }

    private fun getKey(): String {
        val sp = context?.getSharedPreferences("custom_prefs", 0)
        return sp?.getString("key_key", "") ?: ""  // Fetch using the correct key (key_key)
    }

    private fun getBrokerUri(): String {
        val sp = context?.getSharedPreferences("custom_prefs", 0)
        return sp?.getString("host_key", "") ?: ""  // Fetch using the correct key (host_key)
    }

    fun publishMessage(message:String,topic:String){
        if (mqttClient.isConnected){
            try{
                val mqttMessage = MqttMessage()
                mqttMessage.payload = message.toByteArray()
                mqttClient.publish(topic,mqttMessage)
                Log.i("MQTT","publish sent successfully ")
            }catch (e: Exception){
                Log.e("MQTT","Exception during publish: ", e)
            }
        }
    }

    fun setCallback(callback: MqttCallback) {
        mqttClient.setCallback(callback)
    }

}