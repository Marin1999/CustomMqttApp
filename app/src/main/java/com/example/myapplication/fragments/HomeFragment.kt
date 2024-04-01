package com.example.myapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.myapplication.R
import com.example.myapplication.models.BlockTypes
import com.example.myapplication.models.SwitchData
import com.example.myapplication.mqtt.MqttHandler
import com.example.myapplication.touch.SwitchTouchListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEachIndexed
import kotlin.collections.mutableMapOf
import kotlin.collections.set

class HomeFragment : Fragment(R.layout.fragment_home), OnTopicAddedListener {
    private lateinit var mqttHandler: MqttHandler

    private lateinit var switchContainer: FrameLayout
    private lateinit var addBlock: ImageButton
    private lateinit var trashBin: ImageView
    private var editorMode = false
    private val switchMap = mutableMapOf<Switch, SwitchData>()

    private val maxBlocks = 8
    private var blockCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchContainer = view.findViewById(R.id.switchContainer)
        addBlock = view.findViewById(R.id.add_blocks)
        trashBin = view.findViewById(R.id.trashBin)

        val toggleEditorModeButton = view.findViewById<ImageButton>(R.id.editDashboard)

        addBlock.setOnClickListener {
            val topicDialog = AddTopicDialogFragment()
            topicDialog.show(childFragmentManager, "topicDialog")
        }

        toggleEditorModeButton.setOnClickListener {
            toggleEditorMode()
        }

        restoreSwitchesState()

        mqttHandler = MqttHandler(requireContext())

        trashBin.visibility = if(editorMode) View.VISIBLE else View.GONE
    }


    private fun toggleEditorMode() {
        editorMode = !editorMode

        switchContainer.children.forEach { child ->
            if (child is Switch) {
                if (editorMode) {
                    val switchTouchListener =
                        SwitchTouchListener(child, trashBin, switchContainer) { removeSwitch(it) }
                    child.setOnTouchListener(switchTouchListener)
                } else {
                    child.setOnTouchListener(null)

                    val topic = switchMap[child]?.topic ?: ""
                    val x = child.x
                    val y = child.y

                    switchMap[child] = SwitchData(topic, x, y)
                }
            }
        }
        if (editorMode){
            trashBin.visibility = View.VISIBLE
        }else{
            trashBin.visibility = View.GONE
            saveSwitchesState()
        }
    }


    private fun addSwitch(topic: String, blockType: BlockTypes) {
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        Log.i("Blocktype", blockType.toString())



        if (blockCount < maxBlocks) {
            val newSwitch = Switch(requireContext())
            newSwitch.setTrackResource(R.drawable.bg_track)
            newSwitch.setThumbResource(R.drawable.thumb)
            newSwitch.layoutParams = layoutParams
            blockCount++


            val switchTouchListener =
                SwitchTouchListener(newSwitch, trashBin, switchContainer) { removeSwitch(it) }

            newSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mqttHandler.publishMessage("ON",topic)
                }else{
                    mqttHandler.publishMessage("OFF",topic)
                }
                saveSwitchesState()
            }

            if (!editorMode) toggleEditorMode()
            switchMap[newSwitch] = SwitchData( topic, 0f, 0f)
            newSwitch.setOnTouchListener(switchTouchListener)
            switchContainer.addView(newSwitch)
        }else{
            val popup = Toast.makeText(requireContext(),"Maximum switches reached",Toast.LENGTH_SHORT)
            popup.show()
        }
    }

    private fun removeSwitch(switch: Switch) {
        blockCount--

        switchMap.remove(switch)

        switchContainer.removeView(switch)

        saveSwitchesState()


    }

    private fun saveSwitchesState() {
        val sp = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        val editor = sp?.edit()
        editor?.putInt("blockCount", blockCount)

        switchMap.entries.forEachIndexed { index, (switch, switchData) ->
            editor?.putBoolean("switch_$index", switch.isChecked)
            editor?.putString("switch_$index-topic", switchData.topic)
            editor?.putFloat("switch_$index-x", switchData.x)
            editor?.putFloat("switch_$index-y", switchData.y)
        }

        editor?.apply()
    }

    private fun restoreSwitchesState() {
        val sp = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        editorMode = false
        blockCount = sp?.getInt("blockCount", 0) ?: 0

        switchMap.clear()

        for (index in 0 until blockCount) {
            val newSwitch = Switch(requireContext())
            newSwitch.setTrackResource(R.drawable.bg_track)
            newSwitch.setThumbResource(R.drawable.thumb)

            newSwitch.isChecked = sp?.getBoolean("switch_$index", false) ?: false

            val topic = sp?.getString("switch_$index-topic", "") ?: ""
            val x = sp?.getFloat("switch_$index-x", 0f) ?: 0f
            val y = sp?.getFloat("switch_$index-y", 0f) ?: 0f


            newSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mqttHandler.publishMessage("ON",topic)
                }else{
                    mqttHandler.publishMessage("OFF",topic)
                }
                saveSwitchesState()
            }

            switchContainer.addView(newSwitch)

            switchMap[newSwitch] = SwitchData(topic, x, y)

            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.leftMargin = x.toInt()
            layoutParams.topMargin = y.toInt()
            newSwitch.layoutParams = layoutParams
        }

    }

    override fun onTopicAdded(topic: String, blockType: BlockTypes) {
        addSwitch(topic,  blockType)
    }

}