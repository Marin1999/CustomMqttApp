package com.example.myapplication.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.myapplication.R
import com.example.myapplication.models.SwitchData
import com.example.myapplication.touch.SwitchTouchListener
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment(R.layout.fragment_home), OnTopicAddedListener{
    private lateinit var switchContainer: FrameLayout
    private lateinit var addBlock: ImageButton
    private lateinit var trashBin: ImageView
    private var editorMode = false
    private val switchMap = mutableMapOf<Switch,SwitchData>()

    private val maxBlocks = 8
    var blockCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsButton = view.findViewById<FloatingActionButton>(R.id.fab_settings)

        switchContainer = view.findViewById(R.id.switchContainer)
        addBlock = view.findViewById(R.id.add_blocks)
        trashBin = view.findViewById(R.id.trashBin)

        val toggleEditorModeButton = view.findViewById<ImageButton>(R.id.editDashboard)

        settingsButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
            findNavController().navigate(action)
        }

        addBlock.setOnClickListener {
            val topicDialog = AddTopicDialogFragment()
            topicDialog.show(childFragmentManager,"topicDialog")
        }

        toggleEditorModeButton.setOnClickListener {
            toggleEditorMode()
        }

        restoreSwitchesState()
    }


    private fun toggleEditorMode(){
        editorMode = !editorMode

        switchContainer.children.forEach { child ->
            if (child is Switch) {
                if (editorMode) {
                    val switchTouchListener = SwitchTouchListener(child, trashBin, switchContainer){removeSwitch(it)}
                    child.setOnTouchListener(switchTouchListener)
                } else {
                    child.setOnTouchListener(null)

                    val blockName = switchMap[child]?.blockName ?: ""
                    val topic = switchMap[child]?.topic ?: ""
                    val x = child.x
                    val y = child.y

                    switchMap[child] = SwitchData(blockName,topic,x,y)
                }
            }
        }
        if(!editorMode)saveSwitchesState()
    }



    private fun addSwitch(topic: String, blockName: String){
        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        Log.i("Block count",blockCount.toString())



        if (blockCount<maxBlocks){
            val newSwitch = Switch(requireContext())
            newSwitch.text = blockName
            newSwitch.layoutParams = layoutParams
            blockCount++


            val switchTouchListener = SwitchTouchListener(newSwitch, trashBin, switchContainer){removeSwitch(it)}

            newSwitch.setOnCheckedChangeListener{_, isChecked ->
                if (isChecked){
                    val sp = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
                    val Host = sp?.getString("Host","")

                    if (Host != null) {
                        Log.i("switch",Host)
                    }
                }
                saveSwitchesState()
            }

            if (!editorMode) toggleEditorMode()
            switchMap[newSwitch] = SwitchData(blockName,topic,0f,0f)
            newSwitch.setOnTouchListener(switchTouchListener)
            switchContainer.addView(newSwitch)
        }
    }

    private fun removeSwitch(switch: Switch){
        blockCount--

        switchMap.remove(switch)

        switchContainer.removeView(switch)

        saveSwitchesState()


    }

    private fun saveSwitchesState(){
        val sp = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        val editor = sp?.edit()
        editor?.putInt("blockCount",blockCount)

        switchMap.entries.forEachIndexed { index, (switch, switchData) ->
            editor?.putBoolean("switch_$index", switch.isChecked)
            editor?.putString("switch_$index-blockName",switchData.blockName)
            editor?.putString("switch_$index-topic",switchData.topic)
            editor?.putFloat("switch_$index-x", switchData.x)
            editor?.putFloat("switch_$index-y", switchData.y)
        }

        editor?.apply()
    }

    private fun restoreSwitchesState(){
        val sp = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        editorMode = false
        blockCount = sp?.getInt("blockCount",0) ?: 0

        switchMap.clear()

        for (index in 0 until blockCount){
            val newSwitch = Switch(requireContext())

            newSwitch.isChecked = sp?.getBoolean("switch_$index", false) ?: false

            val blockName = sp?.getString("switch_$index-blockName", "") ?: ""
            val topic = sp?.getString("switch_$index-topic","") ?: ""
            val x = sp?.getFloat("switch_$index-x", 0f) ?: 0f
            val y = sp?.getFloat("switch_$index-y", 0f) ?: 0f

            newSwitch.text = blockName

            switchContainer.addView(newSwitch)

            switchMap[newSwitch] = SwitchData(blockName,topic,x,y)

            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.leftMargin = x.toInt()
            layoutParams.topMargin = y.toInt()
            newSwitch.layoutParams = layoutParams
        }

    }

    override fun onTopicAdded(topic: String, blockName: String) {
        addSwitch(topic,blockName)
    }

}