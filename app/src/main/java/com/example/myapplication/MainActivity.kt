package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.myapplication.fragments.HomeFragmentDirections
import com.example.myapplication.fragments.OnTopicAddedListener
import com.example.myapplication.models.BlockTypes
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), OnTopicAddedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.navigation_settings->{
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_home_Fragment_to_settingsFragment)
                    true
                }
                R.id.navigation_home->{
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_settingsFragment_to_home_Fragment)
                    true
                }


                else -> false
            }
        }

    }

    override fun onTopicAdded(topic: String, blockType: BlockTypes) {}
}