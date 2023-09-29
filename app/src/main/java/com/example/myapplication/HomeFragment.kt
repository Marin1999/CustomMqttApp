package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment(R.layout.fragment_home){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsButton = view.findViewById<FloatingActionButton>(R.id.fab_settings)
        settingsButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
            findNavController().navigate(action)
        }

    }
}