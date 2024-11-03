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

        switchContainer = view.findViewById(R.id.switchContainer)
        addBlock = view.findViewById(R.id.add_blocks)
        trashBin = view.findViewById(R.id.trashBin)

        val toggleEditorModeButton = view.findViewById<ImageButton>(R.id.editDashboard)

        addBlock.setOnClickListener {
            val selectBlockTypeFragment = SelectBlockTypeFragment()
            selectBlockTypeFragment.setHomeFragment(this)
            selectBlockTypeFragment.show(parentFragmentManager,"select block type")
        }

        toggleEditorModeButton.setOnClickListener {
            toggleEditorMode()
        }

        restoreBlockState()

        trashBin.visibility = if(editorMode) View.VISIBLE else View.GONE
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



        if (blockCount < maxBlocks) {
            if (blockType == BlockTypes.Switch){
                addSwitch(topic,0f,0f,true)
            }else if (blockType == BlockTypes.Button){
                addButton(topic, 0f, 0f, true)
            }else if (blockType == BlockTypes.Alarm){
                addAlarm(topic,0f,0f,true, time)
            }

        }else{
            val popup = Toast.makeText(requireContext(),"Maximum switches reached",Toast.LENGTH_SHORT)
            popup.show()
        }
    }

    private fun addSwitch(topic: String, x: Float, y: Float, new: Boolean){
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        val newSwitch = Switch(requireContext())
        newSwitch.setTrackResource(R.drawable.bg_track)
        newSwitch.setThumbResource(R.drawable.thumb)
        newSwitch.x = x
        newSwitch.y = y
        newSwitch.layoutParams = layoutParams
        if (new) blockCount++


        val blockTouchListener = BlockTouchListener(newSwitch, trashBin, switchContainer) { removeBlock(it) }

        newSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val mqttHandler = MqttHandler.getInstance(requireContext())
                mqttHandler.publishMessage("ON",topic)
            }else{
                val mqttHandler = MqttHandler.getInstance(requireContext())
                mqttHandler.publishMessage("OFF",topic)
            }
            saveBlockState()
        }

        if (!editorMode && new) toggleEditorMode()
        blockMap[newSwitch] = BlockData(topic, x, y)
        if (editorMode) newSwitch.setOnTouchListener(blockTouchListener)
        switchContainer.addView(newSwitch)
    }

    private fun addButton(topic: String, x: Float, y: Float, new: Boolean){
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        val newButton = Button(requireContext())
        newButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
        newButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        newButton.layoutParams = layoutParams
        newButton.x = x
        newButton.y = y
        if (new) blockCount++

        newButton.setOnClickListener {
            val mqttHandler = MqttHandler.getInstance(requireContext())
            mqttHandler.publishMessage("1",topic)
        }

        if (!editorMode && new) toggleEditorMode()

        val blockTouchListener = BlockTouchListener(newButton, trashBin, switchContainer) { removeBlock(it) }

        blockMap[newButton] = BlockData(topic,x,y)
        if (editorMode) newButton.setOnTouchListener(blockTouchListener)
        newButton.text = topic.substringAfterLast("/")

        switchContainer.addView(newButton)


    }

    private fun addAlarm(topic: String,x: Float,y: Float,new: Boolean,time: Long){
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(Date(time))



        val newAlarm = AlarmView(requireContext())
        newAlarm.text = "Alarm set at $formattedTime"
        newAlarm.layoutParams = layoutParams
        newAlarm.x = x
        newAlarm.y = y

        if (new) blockCount++
        if (!editorMode && new) toggleEditorMode()

        val blockTouchListener = BlockTouchListener(newAlarm, trashBin, switchContainer) { removeBlock(it) }

        blockMap[newAlarm] = BlockData(topic, x, y, time)



        if (editorMode) newAlarm.setOnTouchListener(blockTouchListener)

        switchContainer.addView(newAlarm)

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("topic", topic)
        }
        val uniqueRequestCode = UUID.randomUUID().hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm to trigger at the specified time
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }

    private fun removeBlock(block: Any) {
        blockCount--

        blockMap.remove(block)

        if (block is View) {
            switchContainer.removeView(block)
        }

        if (block is AlarmView) {
            switchContainer.removeView(block)
        }

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