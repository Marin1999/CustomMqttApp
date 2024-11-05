package com.example.myapplication.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.models.BlockTypes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

interface OnTopicAddedListener {
    fun onTopicAdded(topic: String,  blockType: BlockTypes, time: Long)
}

class AddTopicDialogFragment(selectedBlockType: BlockTypes) : BottomSheetDialogFragment() {
    private var onTopicAddedListener: OnTopicAddedListener? = null
    private var selectedBlockType: BlockTypes = selectedBlockType

    private var parentFragment: Fragment? = null
    private lateinit var homeFragment: HomeFragment


    fun setParentFragment(fragment: Fragment) {
        parentFragment = fragment
    }

    fun setHomeFragment(fragment: HomeFragment) {
        homeFragment = fragment
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OnTopicAddedListener) {
            onTopicAddedListener = parentFragment as OnTopicAddedListener
        } else {
            throw ClassCastException("$context must implement OnTopicAddedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View


        if (selectedBlockType == BlockTypes.Alarm) {
            view = inflater.inflate(R.layout.topic_dialog_alarm, container, false) // Different layout for Alarm
        } else {
            view = inflater.inflate(R.layout.topic_dialog_default, container, false) // Default layout
        }

        setupConfirmButton(view)
        return view
    }
    private fun setupConfirmButton(view: View) {
        val topicInput = view.findViewById<EditText>(R.id.editTopic)
        val confirmButton = view.findViewById<AppCompatButton>(R.id.createButton)

        confirmButton.setOnClickListener {
            val topic = topicInput.text.toString()
            if (topic.isBlank()) {
                Toast.makeText(requireContext(), "Topic cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedBlockType == BlockTypes.Alarm) {
                handleAlarmTopic(view, topic)
            } else {
                homeFragment?.onTopicAdded(topic, selectedBlockType, 0L)
                dismiss()
            }
        }
    }

    private fun handleAlarmTopic(view: View, topic: String) {
        val hourInput = view.findViewById<EditText>(R.id.editHours)
        val minuteInput = view.findViewById<EditText>(R.id.editMinutes)

        val hour = hourInput.text.toString().toIntOrNull()
        val minute = minuteInput.text.toString().toIntOrNull()

        if (hour != null && minute != null && hour in 0..23 && minute in 0..59) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val selectedTimeInMillis = calendar.timeInMillis

            homeFragment?.onTopicAdded(topic, BlockTypes.Alarm, selectedTimeInMillis)
            dismiss()
        } else {
            Toast.makeText(requireContext(), "Please enter a valid hour (0-23) and minute (0-59)", Toast.LENGTH_SHORT).show()
        }
    }

}