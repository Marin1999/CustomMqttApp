package com.example.myapplication.fragments

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.touch.SwitchTouchListener
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment(R.layout.fragment_home){
    private lateinit var switchContainer: FrameLayout
    private lateinit var addSwitch: ImageButton
    private lateinit var trashBin: ImageView

    private val maxSwitches = 6
    var switchCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigateToSettings(view)
        addSwitches(view)

    }

    private fun navigateToSettings(view: View){
        val settingsButton = view.findViewById<FloatingActionButton>(R.id.fab_settings)
        settingsButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
    }

    private fun addSwitches(view: View){
        switchContainer = view.findViewById(R.id.switchContainer)
        addSwitch = view.findViewById(R.id.add_blocks)
        trashBin = view.findViewById(R.id.trashBin)

        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        addSwitch.setOnClickListener {
            if (switchCount<maxSwitches){
                val newSwitch = Switch(requireContext())
                newSwitch.text = "Switch ${switchCount+1}"
                newSwitch.layoutParams = layoutParams
                val switchTouchListener = SwitchTouchListener(newSwitch,trashBin,switchContainer)
                newSwitch.setOnTouchListener(switchTouchListener)
                switchContainer.addView(newSwitch)
                switchCount++
            }
        }
    }


}