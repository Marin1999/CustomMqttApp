package com.example.myapplication.data.models

data class BlockData(
    val type: Enum<BlockTypes>,
    val topic: String,
    var x: Float,
    var y: Float,
    val isChecked: Boolean? = null,
    val time: Long? = null
)