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
        Log.d("ParentFragment", "Parent fragment: ${parentFragment?.javaClass?.simpleName}")
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
        val editHour = view.findViewById<EditText>(R.id.editHours)
        val editMinute = view.findViewById<EditText>(R.id.editMinutes)



        val confirmButton = view.findViewById<AppCompatButton>(R.id.createButton)
        confirmButton.setOnClickListener {
            val topic = view.findViewById<EditText>(R.id.editTopic).text.toString()




            if (selectedBlockType == BlockTypes.Alarm) {
                val hour = editHour.text.toString()
                val minute = editMinute.text.toString()

                if (hour.isNotEmpty() && minute.isNotEmpty()) {
                    val calendar = Calendar.getInstance()

                    calendar.set(Calendar.HOUR_OF_DAY, hour.toInt())
                    calendar.set(Calendar.MINUTE, minute.toInt())
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val selectedTimeInMillis = calendar.timeInMillis

                    homeFragment.onTopicAdded(topic, selectedBlockType, selectedTimeInMillis)
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Please enter valid hour and minute", Toast.LENGTH_SHORT).show()
                }
            } else {
                homeFragment.onTopicAdded(topic, selectedBlockType, 0L)
                dismiss()
            }


        }
        return view
    }
}