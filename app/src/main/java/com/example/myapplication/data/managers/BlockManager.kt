package com.example.myapplication.data.managers

import android.content.SharedPreferences
import android.widget.Button
import android.widget.Switch
import com.example.myapplication.data.models.BlockData
import com.example.myapplication.data.models.BlockTypes
import com.example.myapplication.ui.views.AlarmView
import javax.inject.Inject

class BlockManager @Inject constructor() {
    private val maxBlocks = 8
    private var blockCount = 0

    private val blocks = mutableMapOf<Any, BlockData>()

    fun blockLimitReached(): Boolean{
        return blockCount >= maxBlocks
    }

    fun addBlock(view: Any, data: BlockData) {
        blocks[view] = data
        blockCount++
    }

    fun removeBlock(view: Any) {
        blocks.remove(view)
        blockCount--
    }

    fun updateBlock(view: Any, data: BlockData){
        blocks[view] = data
    }

    fun saveState(prefs: SharedPreferences) {
        val editor = prefs.edit()
        editor?.putInt("blockCount", blockCount)

        blocks.entries.forEachIndexed { index, (any, blockData) ->
            if (any is Switch) {
                editor?.putString("block_$index-type", "switch")
                editor?.putBoolean("block_$index-checked", any.isChecked)
            } else if (any is Button) {
                editor?.putString("block_$index-type", "button")
            } else if (any is AlarmView) {
                editor?.putString("block_$index-type", "alarm")
                blockData.time?.let { editor?.putLong("block_$index-time", it) }
            }
            editor?.putString("block_$index-topic", blockData.topic)
            editor?.putFloat("block_$index-x", blockData.x)
            editor?.putFloat("block_$index-y", blockData.y)
        }

        editor?.apply()
    }

    fun restoreState(prefs: SharedPreferences) : List<BlockData>{
        val restoreBlocks = mutableListOf<BlockData>()
        val count = prefs.getInt("blockCount", 0)

        for (index in 0 until count) {
            val type = prefs.getString("block_${index}-type", null) ?: continue
            val topic = prefs.getString("block_${index}-topic", null) ?: continue
            val x = prefs.getFloat("block_${index}-x", 0f)
            val y = prefs.getFloat("block_${index}-y", 0f)

            val data = when (type) {
                "switch" -> {
                    val checked = prefs.getBoolean("block_${index}-checked", false)
                    BlockData(BlockTypes.Switch,topic, x, y, isChecked = checked)
                }
                "alarm" -> {
                    val time = prefs.getLong("block_${index}-time", 0L)
                    BlockData(BlockTypes.Alarm, topic, x, y, time = time)
                }
                "button" -> BlockData(BlockTypes.Button, topic, x, y)
                else -> null
            }

            data?.let { restoreBlocks.add(it) }
        }

        return restoreBlocks
    }

    fun getBlockData(view: Any): BlockData? {
        return blocks[view]
    }

}