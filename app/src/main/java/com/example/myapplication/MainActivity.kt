package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.fragments.OnTopicAddedListener

class MainActivity : AppCompatActivity(), OnTopicAddedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onTopicAdded(topic: String, blockName: String) {}
}