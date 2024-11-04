package com.example.myapplication.fragments

import android.content.Context
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
    ): View {
        val rootView = inflater.inflate(R.layout.custom_preference_screen, container, false)
        sharedPreferences = requireContext().getSharedPreferences("custom_prefs", Context.MODE_PRIVATE)

        editHost = rootView.findViewById(R.id.editHost)
        editUsername = rootView.findViewById(R.id.editUsername)
        editKey = rootView.findViewById(R.id.editKey)

        loadPreferences()

        setupPreferenceSaveListener(editHost, "host_key")
        setupPreferenceSaveListener(editUsername, "username_key")
        setupPreferenceSaveListener(editKey, "key_key")

        return rootView
    }

    private fun loadPreferences() {
        editHost.setText(sharedPreferences.getString("host_key", ""))
        editUsername.setText(sharedPreferences.getString("username_key", ""))
        editKey.setText(sharedPreferences.getString("key_key", ""))
    }

    private fun setupPreferenceSaveListener(editText: EditText, key: String) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                savePreference(key, editText.text.toString())
            }
        }
    }

    private fun savePreference(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
}
