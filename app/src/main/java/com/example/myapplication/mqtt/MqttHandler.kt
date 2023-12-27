package com.example.myapplication.mqtt

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

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
                exception?.printStackTrace()
            }
        })
    }

    private fun getUsername(): String{
        val sp = context.let { PreferenceManager.getDefaultSharedPreferences(it) }
        return ""
    }

    private fun getKey(): String{
        val sp = context.let { PreferenceManager.getDefaultSharedPreferences(it) }
        return ""
    }

    private fun getBrokerUri(): String {
        val sp = context.let { PreferenceManager.getDefaultSharedPreferences(it) }
        return sp?.getString("Host", "") ?: ""
    }

    fun publishMessage(message:String,topic:String){
        if (mqttClient.isConnected){
            val mqttMessage = MqttMessage()
            mqttMessage.payload = message.toByteArray()
            mqttClient.publish(topic,mqttMessage)
        }
    }

    fun setCallback(callback: MqttCallback) {
        mqttClient.setCallback(callback)
    }

}