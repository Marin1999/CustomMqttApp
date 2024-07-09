package com.example.myapplication.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.myapplication.R
import com.example.myapplication.models.BlockTypes

interface OnTopicAddedListener {
    fun onTopicAdded(topic: String,  blockType: BlockTypes)
}

class AddTopicDialogFragment(selectedBlockType: BlockTypes) : DialogFragment() {
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
        val view = inflater.inflate(R.layout.topic_dialog, container, false)
        val confirmButton = view.findViewById<ImageButton>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            val topic = view.findViewById<EditText>(R.id.editTopic).text.toString()

            homeFragment.onTopicAdded(topic,  selectedBlockType)
            dismiss()
        }
        return view
    }
}