package com.example.myapplication.models

data class BlockData(
    val topic: String,
    var x: Float,
    var y: Float,
    val time: Long? = null
)