package com.example.myapplication.ui.fragments

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
import com.example.myapplication.data.managers.BlockManager
import com.example.myapplication.data.models.BlockData
import com.example.myapplication.data.models.BlockTypes
import com.example.myapplication.data.network.mqtt.MqttHandler
import com.example.myapplication.receivers.AlarmReceiver
import com.example.myapplication.ui.interactions.BlockTouchListener
import com.example.myapplication.ui.views.AlarmView
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), OnTopicAddedListener {

    @Inject
    lateinit var mqttHandler: MqttHandler

    @Inject
    lateinit var blockManager: BlockManager

    private lateinit var switchContainer: FrameLayout
    private lateinit var addBlock: ImageButton
    private lateinit var trashBin: ImageView
    private var editorMode = false


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



                    val oldData = blockManager.getBlockData(child)
                    if (oldData != null) {
                        val updatedBlock = oldData.copy(x = child.x, y = child.y)
                        blockManager.updateBlock(child, updatedBlock)
                    }
                }
            }
        }
        if (editorMode) {
            trashBin.visibility = View.VISIBLE
        } else {
            trashBin.visibility = View.GONE
            saveBlockState()
        }
    }


    private fun addBlock(topic: String, blockType: BlockTypes, time: Long) {
        Log.i("Blocktype", blockType.toString())

        if (blockManager.blockLimitReached()) {
            Toast.makeText(requireContext(), "Maximum blocks reached", Toast.LENGTH_SHORT).show()
            return
        }

        when (blockType) {
            BlockTypes.Switch -> addSwitch(topic, 0f, 0f, false, true)
            BlockTypes.Button -> addButton(topic, 0f, 0f, true)
            BlockTypes.Alarm -> addAlarm(topic, 0f, 0f, true, time)
        }
    }

    private fun addSwitch(topic: String, x: Float, y: Float, isChecked: Boolean, isNew: Boolean) {
        val newSwitch = Switch(requireContext()).apply {
            setTrackResource(R.drawable.bg_track)
            setThumbResource(R.drawable.thumb)
            this.x = x
            this.y = y
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            configureSwitch(this, topic, isChecked)
        }
        addViewToContainer(newSwitch, isNew)
        if (isNew){
            blockManager.addBlock(newSwitch, BlockData(BlockTypes.Switch, topic, x, y))
        }

    }

    private fun configureSwitch(newSwitch: Switch, topic: String, isChecked: Boolean) {
        newSwitch.setOnCheckedChangeListener { _, isChecked ->
            mqttHandler.publishMessage(if (isChecked) "ON" else "OFF", topic)
            saveBlockState()
        }
        newSwitch.isChecked = isChecked
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
                mqttHandler.publishMessage("1", topic)
            }
        }
        addViewToContainer(newButton, isNew)
        if (isNew){
            blockManager.addBlock(newButton, BlockData(BlockTypes.Button, topic, x, y))
        }
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
        if (isNew) {
            blockManager.addBlock(newAlarm, BlockData(BlockTypes.Alarm, topic, x, y, time = time))
        }
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
        if (!editorMode && isNew) toggleEditorMode()

        view.setOnTouchListener(if (editorMode) createBlockTouchListener(view) else null)
        switchContainer.addView(view)
    }

    private fun createBlockTouchListener(view: View): View.OnTouchListener {
        return BlockTouchListener(view, trashBin, switchContainer) { removeBlock(it) }
    }


    private fun removeBlock(block: Any) {
        blockManager.removeBlock(block)
        if (block is View) switchContainer.removeView(block)
        saveBlockState()
    }

    private fun saveBlockState() {
        val prefs = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        if (prefs != null) {
            blockManager.saveState(prefs)
        }
    }

    private fun restoreBlockState() {
        val prefs = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        editorMode = false
        for (block in prefs?.let { blockManager.restoreState(it) }!!) {
            when (block.type) {
                BlockTypes.Switch -> addSwitch(
                    block.topic,
                    block.x,
                    block.y,
                    block.isChecked == true,
                    false
                )

                BlockTypes.Button -> addButton(block.topic, block.x, block.y, false)
                BlockTypes.Alarm -> addAlarm(block.topic, block.x, block.y, false, block.time ?: 0L)
            }
        }

    }


    override fun onTopicAdded(topic: String, blockType: BlockTypes, time: Long) {
        addBlock(topic, blockType, time)
    }

}