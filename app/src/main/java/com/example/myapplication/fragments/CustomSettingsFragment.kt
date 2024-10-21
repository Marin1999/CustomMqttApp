package com.example.myapplication.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class CustomPreferenceFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editHost: EditText
    private lateinit var editUsername: EditText
    private lateinit var editKey: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.custom_preference_screen, container, false)

        sharedPreferences = requireContext().getSharedPreferences("custom_prefs", 0)

        editHost = rootView.findViewById(R.id.editHost)
        editUsername = rootView.findViewById(R.id.editUsername)
        editKey = rootView.findViewById(R.id.editKey)

        val savedHost = sharedPreferences.getString("host_key", "")
        val savedUsername = sharedPreferences.getString("username_key", "")
        val savedKey = sharedPreferences.getString("key_key", "")

        editHost.setText(savedHost)
        editUsername.setText(savedUsername)
        editKey.setText(savedKey)

        editHost.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                savePreference("host_key", editHost.text.toString())
            }
        }

        editUsername.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                savePreference("username_key", editUsername.text.toString())
            }
        }

        editKey.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                savePreference("key_key", editKey.text.toString())
            }
        }

        return rootView
    }

    private fun savePreference(key: String, value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }
}