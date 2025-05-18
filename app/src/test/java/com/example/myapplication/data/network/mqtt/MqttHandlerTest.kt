package com.example.myapplication.data.network.mqtt

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import info.mqtt.android.service.MqttAndroidClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.junit.Before
import org.junit.Test


internal class MqttHandlerTest {
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mqttClient: MqttAndroidClient

    private lateinit var mqttHandler: MqttHandler


    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        mqttClient = mockk(relaxed = true)

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0

        val mqttToken = mockk<IMqttToken>(relaxed = true)

        mockkStatic(EncryptedSharedPreferences::class)
        mockkConstructor(MasterKey.Builder::class)
        mockkConstructor(MqttAndroidClient::class)

        every {
            EncryptedSharedPreferences.create(
                any<Context>(),
                any(),
                any(),
                any(),
                any()
            )
        } returns sharedPreferences

        every {
            anyConstructed<MqttAndroidClient>().connect(any(), any(), any())
        } returns mqttToken


        mqttHandler = MqttHandler(context)

    }

    @Test
    fun publishMessage() {
        //given
        val message = "ON"
        val topic = "test/topic"

        val mockToken = mockk<IMqttDeliveryToken>(relaxed = true)

        //when
        every { anyConstructed<MqttAndroidClient>().isConnected } returns true
        every { anyConstructed<MqttAndroidClient>().publish(any(),any()) } returns mockToken

        mqttHandler.publishMessage(message, topic)

        //then
        verify {
            anyConstructed<MqttAndroidClient>().publish(
                topic,
                withArg { mqttMessage ->
                    assert(String(mqttMessage.payload) == message)
                }
            )
        }


    }
}