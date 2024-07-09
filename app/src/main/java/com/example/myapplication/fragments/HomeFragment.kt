package com.example.myapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.myapplication.R
import com.example.myapplication.models.BlockTypes
import com.example.myapplication.models.BlockData
import com.example.myapplication.mqtt.MqttHandler
import com.example.myapplication.touch.BlockTouchListener
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

        mqttHandler = MqttHandler(requireContext())

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

                    blockMap[child] = BlockData(topic, x, y)
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


    private fun addBlock(topic: String, blockType: BlockTypes) {
        Log.i("Blocktype", blockType.toString())



        if (blockCount < maxBlocks) {
            if (blockType == BlockTypes.Switch){
                addSwitch(topic,0f,0f,true)
            }else if (blockType == BlockTypes.Button){
                addButton(topic, 0f, 0f, true)
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
                mqttHandler.publishMessage("ON",topic)
            }else{
                mqttHandler.publishMessage("OFF",topic)
            }
            saveBlockState()
        }

        if (!editorMode && new) toggleEditorMode()
        blockMap[newSwitch] = BlockData(topic, x, y)
        newSwitch.setOnTouchListener(blockTouchListener)
        switchContainer.addView(newSwitch)
    }

    private fun addButton(topic: String, x: Float, y: Float, new: Boolean){
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        val newButton = Button(requireContext())
        newButton.layoutParams = layoutParams
        newButton.x = x
        newButton.y = y
        if (new) blockCount++

        newButton.setOnClickListener {
            mqttHandler.publishMessage("1",topic)
        }

        if (!editorMode && new) toggleEditorMode()

        val blockTouchListener = BlockTouchListener(newButton, trashBin, switchContainer) { removeBlock(it) }

        blockMap[newButton] = BlockData(topic,x,y)
        newButton.setOnTouchListener(blockTouchListener)
        newButton.text = topic.substringAfterLast("/")

        switchContainer.addView(newButton)


    }

    private fun removeBlock(block: Any) {
        blockCount--

        blockMap.remove(block)

        if (block is View) {
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
            }

        }

    }

    override fun onTopicAdded(topic: String, blockType: BlockTypes) {
        addBlock(topic,  blockType)
    }

}