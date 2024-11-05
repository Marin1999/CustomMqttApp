package com.example.myapplication.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.myapplication.R
import com.example.myapplication.models.BlockTypes
import com.example.myapplication.models.BlockData
import com.example.myapplication.mqtt.MqttHandler
import com.example.myapplication.receivers.AlarmReceiver
import com.example.myapplication.touch.BlockTouchListener
import com.example.myapplication.views.AlarmView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEachIndexed
import kotlin.collections.mutableMapOf
import kotlin.collections.set

class HomeFragment : Fragment(R.layout.fragment_home), OnTopicAddedListener {

    private lateinit var switchContainer: FrameLayout
    private lateinit var addBlock: ImageButton
    private lateinit var trashBin: ImageView
    private var editorMode = false
    private val blockMap = mutableMapOf<Any, BlockData>()

    private val maxBlocks = 8
    private var blockCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI(view)

        restoreBlockState()

    }

    private fun setupUI(view: View) {
        switchContainer = view.findViewById(R.id.switchContainer)
        addBlock = view.findViewById(R.id.add_blocks)
        trashBin = view.findViewById(R.id.trashBin)

        val toggleEditorModeButton = view.findViewById<ImageButton>(R.id.editDashboard)

        addBlock.setOnClickListener {
            val selectBlockTypeFragment = SelectBlockTypeFragment().apply {
                setHomeFragment(this@HomeFragment)
            }
            selectBlockTypeFragment.show(parentFragmentManager, "select block type")
        }

        toggleEditorModeButton.setOnClickListener { toggleEditorMode() }
        trashBin.visibility = if (editorMode) View.VISIBLE else View.GONE
    }


    private fun toggleEditorMode() {
        editorMode = !editorMode

        switchContainer.children.forEach { child ->
            if (child is View) {
                if (editorMode) {
                    val blockTouchListener =
                        BlockTouchListener(child, trashBin, switchContainer) { removeBlock(it) }
                    child.setOnTouchListener(blockTouchListener)
                } else {
                    child.setOnTouchListener(null)

                    val topic = blockMap[child]?.topic ?: ""
                    val x = child.x
                    val y = child.y
                    val time = blockMap[child]?.time

                    blockMap[child] = BlockData(topic, x, y, time)
                }
            }
        }
        if (editorMode){
            trashBin.visibility = View.VISIBLE
        }else{
            trashBin.visibility = View.GONE
            saveBlockState()
        }
    }


    private fun addBlock(topic: String, blockType: BlockTypes, time: Long) {
        Log.i("Blocktype", blockType.toString())



        if (blockCount >= maxBlocks) {
            Toast.makeText(requireContext(), "Maximum blocks reached", Toast.LENGTH_SHORT).show()
            return
        }

        when (blockType) {
            BlockTypes.Switch -> addSwitch(topic, 0f, 0f, true)
            BlockTypes.Button -> addButton(topic, 0f, 0f, true)
            BlockTypes.Alarm -> addAlarm(topic, 0f, 0f, true, time)
        }
    }

    private fun addSwitch(topic: String, x: Float, y: Float, isNew: Boolean) {
        val newSwitch = Switch(requireContext()).apply {
            setTrackResource(R.drawable.bg_track)
            setThumbResource(R.drawable.thumb)
            this.x = x
            this.y = y
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            configureSwitch(this, topic)
        }
        addViewToContainer(newSwitch,  isNew)
        blockMap[newSwitch] = BlockData(topic, x, y )

    }

    private fun configureSwitch(newSwitch: Switch, topic: String) {
        newSwitch.setOnCheckedChangeListener { _, isChecked ->
            val mqttHandler = MqttHandler.getInstance(requireContext())
            mqttHandler.publishMessage(if (isChecked) "ON" else "OFF", topic)
            saveBlockState()
        }
    }

    private fun addButton(topic: String, x: Float, y: Float, isNew: Boolean) {
        val newButton = Button(requireContext()).apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            this.x = x
            this.y = y
            text = topic.substringAfterLast("/")
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                val mqttHandler = MqttHandler.getInstance(requireContext())
                mqttHandler.publishMessage("1", topic)
            }
        }
        addViewToContainer(newButton, isNew)
        blockMap[newButton] = BlockData(topic, x, y)
    }

    private fun addAlarm(topic: String, x: Float, y: Float, isNew: Boolean, time: Long) {
        val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(time))
        val newAlarm = AlarmView(requireContext()).apply {
            text = "Alarm set at $formattedTime"
            this.x = x
            this.y = y
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
        addViewToContainer(newAlarm, isNew)
        blockMap[newAlarm] = BlockData(topic, x, y, time)
        setAlarm(topic, time)
    }

    private fun setAlarm(topic: String, time: Long) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("topic", topic)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            UUID.randomUUID().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }

    private fun addViewToContainer(view: View, isNew: Boolean) {
        if (isNew) blockCount++
        if (!editorMode && isNew) toggleEditorMode()

        view.setOnTouchListener(if (editorMode) createBlockTouchListener(view) else null)
        switchContainer.addView(view)
    }

    private fun createBlockTouchListener(view: View): View.OnTouchListener {
        return BlockTouchListener(view, trashBin, switchContainer) { removeBlock(it) }
    }


    private fun removeBlock(block: Any) {
        blockCount--
        blockMap.remove(block)
        if (block is View) switchContainer.removeView(block)
        saveBlockState()
    }

    private fun saveBlockState() {
        val sp = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        val editor = sp?.edit()
        editor?.putInt("blockCount", blockCount)

        blockMap.entries.forEachIndexed { index, (any, blockData) ->
            if (any is Switch){
                editor?.putString("block_$index-type","switch")
                editor?.putBoolean("block_$index-checked", any.isChecked)
            }else if(any is Button){
                editor?.putString("block_$index-type","button")
            }else if(any is AlarmView){
                editor?.putString("block_$index-type","alarm")
                blockData.time?.let { editor?.putLong("block_$index-time", it) }
            }
            editor?.putString("block_$index-topic", blockData.topic)
            editor?.putFloat("block_$index-x", blockData.x)
            editor?.putFloat("block_$index-y", blockData.y)
        }

        editor?.apply()
    }

    private fun restoreBlockState() {
        val sp = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        editorMode = false
        blockCount = sp?.getInt("blockCount", 0) ?: 0

        Log.i("Restore Blocks", "restoring blocks with block count of $blockCount")

        blockMap.clear()

        for (index in 0 until blockCount) {
            val type = sp?.getString("block_$index-type","")?:""
            val topic = sp?.getString("block_$index-topic", "") ?: ""
            val x = sp?.getFloat("block_$index-x", 0f) ?: 0f
            val y = sp?.getFloat("block_$index-y", 0f) ?: 0f

            if (type == "switch"){
                addSwitch(topic,x,y,false)
            }else if (type == "button"){
                addButton(topic,x,y, false)
            }else if (type == "alarm"){
                val time = sp?.getLong("block_$index-time", 0) ?: 0
                addAlarm(topic,x,y,false, time)
            }
        }

    }


    override fun onTopicAdded(topic: String, blockType: BlockTypes, time:Long) {
        addBlock(topic,  blockType, time)
    }

}