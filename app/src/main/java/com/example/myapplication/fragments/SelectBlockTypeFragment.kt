package com.example.myapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.models.BlockTypes

class SelectBlockTypeFragment:DialogFragment(), OnTopicAddedListener {
    private lateinit var homeFragment: HomeFragment

    fun setHomeFragment(fragment: HomeFragment) {
        homeFragment = fragment
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.block_selector, container, false)
        val selectButton = view.findViewById<ImageButton>(R.id.select_button)
        selectButton.setOnClickListener{
            Log.d("ParentFragment", "Parent fragment: ${parentFragment?.javaClass?.simpleName}")
            val topicDialog = AddTopicDialogFragment()
            topicDialog.setHomeFragment(homeFragment)
            topicDialog.setParentFragment(this)
            topicDialog.show(parentFragmentManager,"topic")
            dismiss()
        }
        return view
    }

    override fun onTopicAdded(topic: String, blockType: BlockTypes) {
        Log.i("wtf","it is in wrong dialog")
    }
}