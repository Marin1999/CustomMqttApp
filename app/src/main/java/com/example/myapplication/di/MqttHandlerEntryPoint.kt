package com.example.myapplication.di

import com.example.myapplication.data.network.mqtt.MqttHandler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MqttHandlerEntryPoint {
    fun mqttHandler() : MqttHandler
}