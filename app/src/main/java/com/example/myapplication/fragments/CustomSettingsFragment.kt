package com.example.myapplication.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.myapplication.R

class CustomPreferenceFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editHost: EditText
    private lateinit var editUsername: EditText
    private lateinit var editKey: EditText
    private lateinit var checkBox: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.custom_preference_screen, container, false)

        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            requireContext(),
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        editHost = rootView.findViewById(R.id.editHost)
        editUsername = rootView.findViewById(R.id.editUsername)
        editKey = rootView.findViewById(R.id.editKey)
        checkBox = rootView.findViewById(R.id.checkBoxAuthentication)

        loadPreferences()

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            toggleFields(isChecked)
            savePreference("checkbox_key", isChecked.toString())
        }

        setupPreferenceSaveListener(editHost, "host_key")
        setupPreferenceSaveListener(editUsername, "username_key")
        setupPreferenceSaveListener(editKey, "key_key")

        return rootView
    }

    private fun loadPreferences() {
        editHost.setText(sharedPreferences.getString("host_key", ""))
        editUsername.setText(sharedPreferences.getString("username_key", ""))
        editKey.setText(sharedPreferences.getString("key_key", ""))
        val isCheckboxChecked = sharedPreferences.getString("checkbox_key", "false")?.toBoolean() ?: false
        checkBox.isChecked = isCheckboxChecked
        toggleFields(isCheckboxChecked)
    }

    private fun setupPreferenceSaveListener(editText: EditText, key: String) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                savePreference(key, editText.text.toString())
            }
        }
    }
    private fun toggleFields(enable: Boolean) {
        editUsername.isEnabled = enable
        editKey.isEnabled = enable
    }

    private fun savePreference(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
}
