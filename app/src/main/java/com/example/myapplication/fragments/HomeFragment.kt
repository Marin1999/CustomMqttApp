package com.example.myapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.myapplication.R
import com.example.myapplication.touch.SwitchTouchListener
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment(R.layout.fragment_home), OnTopicAddedListener{
    private lateinit var switchContainer: FrameLayout
    private lateinit var addBlock: ImageButton
    private lateinit var trashBin: ImageView

    private val maxBlocks = 8
    var blockCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigateToSettings(view)
        addBlock(view)

    }

    private fun navigateToSettings(view: View){
        val settingsButton = view.findViewById<FloatingActionButton>(R.id.fab_settings)
        settingsButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
    }

    private fun addBlock(view: View){
        switchContainer = view.findViewById(R.id.switchContainer)
        addBlock = view.findViewById(R.id.add_blocks)
        trashBin = view.findViewById(R.id.trashBin)

        addBlock.setOnClickListener {
            val topicDialog = AddTopicDialogFragment()
            topicDialog.show(childFragmentManager,"topicDialog")
        }
    }

    private fun addSwitch(topic: String, blockName: String){
        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        if (blockCount<maxBlocks){
            val newSwitch = Switch(requireContext())
            newSwitch.text = blockName
            newSwitch.layoutParams = layoutParams
            val switchTouchListener = SwitchTouchListener(newSwitch,trashBin,switchContainer)
            newSwitch.setOnCheckedChangeListener{_, isChecked ->
                if (isChecked){
                    val sp = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
                    val Host = sp?.getString("Host","")

                    if (Host != null) {
                        Log.i("switch",Host)
                    }
                }else{

                }


            }

            newSwitch.setOnTouchListener(switchTouchListener)
            switchContainer.addView(newSwitch)
            blockCount++
        }
    }


    override fun onTopicAdded(topic: String, blockName: String) {
        addSwitch(topic,blockName)
    }


}