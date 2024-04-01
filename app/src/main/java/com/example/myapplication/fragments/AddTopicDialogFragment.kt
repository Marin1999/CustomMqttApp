package com.example.myapplication.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.models.BlockTypes

interface OnTopicAddedListener {
    fun onTopicAdded(topic: String,  blockType: BlockTypes)
}

class AddTopicDialogFragment : DialogFragment() {
    private var onTopicAddedListener: OnTopicAddedListener? = null
    private var selectedBlockType: BlockTypes = BlockTypes.Switch

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
        val view = inflater.inflate(R.layout.topic_dialog, container, false)
        setupSpinner(view)
        val confirmButton = view.findViewById<ImageButton>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            val topic = view.findViewById<EditText>(R.id.editTopic).text.toString()


            onTopicAddedListener?.onTopicAdded(topic,  selectedBlockType)
            dismiss()
        }
        return view
    }

    private fun setupSpinner(view:View){
        val spinner = view.findViewById<Spinner>(R.id.selectBlock)
        val blockOptions = resources.getStringArray(R.array.block_types)

        if (spinner != null){
            val adapter = ArrayAdapter(requireContext(), androidx.transition.R.layout.support_simple_spinner_dropdown_item,blockOptions)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedBlockType = when (position) {
                        0 -> BlockTypes.Switch
                        1 -> BlockTypes.Button
                        else -> BlockTypes.Switch
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }



            }
        }
    }
}