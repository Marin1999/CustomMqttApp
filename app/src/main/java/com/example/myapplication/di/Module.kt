package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.managers.BlockManager
import com.example.myapplication.data.network.mqtt.MqttHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class Module {

    @Provides
    @Singleton
    fun provideMqttHandler(
        @ApplicationContext context: Context
    ): MqttHandler {
        return MqttHandler(context)
    }

    @Provides
    @Singleton
    fun provideBlockManager(
    ): BlockManager {
        return BlockManager()
    }
}