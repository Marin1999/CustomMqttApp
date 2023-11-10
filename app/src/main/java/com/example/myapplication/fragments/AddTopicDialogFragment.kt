package com.example.myapplication.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R

interface OnTopicAddedListener {
    fun onTopicAdded(topic: String, blockName: String)
}

class AddTopicDialogFragment : DialogFragment() {
    private var onTopicAddedListener: OnTopicAddedListener? = null

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
        val view = inflater.inflate(R.layout.topic_dialog,container,false)

        val confirmButton = view.findViewById<ImageButton>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            val topic = view.findViewById<EditText>(R.id.editTopic).text.toString()
            val blockName = view.findViewById<EditText>(R.id.editBlockName).text.toString()

            onTopicAddedListener?.onTopicAdded(topic,blockName)
            dismiss()
        }
        return view
    }
}