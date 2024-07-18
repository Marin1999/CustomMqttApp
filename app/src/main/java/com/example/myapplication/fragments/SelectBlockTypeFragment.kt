package com.example.myapplication.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.models.BlockTypes

class SelectBlockTypeFragment:DialogFragment(), OnTopicAddedListener, View.OnClickListener {
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
        view.findViewById<ImageButton>(R.id.select_switch).setOnClickListener(this)
        view.findViewById<ImageButton>(R.id.select_button).setOnClickListener(this)
        view.findViewById<ImageButton>(R.id.select_alarm).setOnClickListener(this)

        return view
    }
    override fun onClick(p0: View?) {
        when (p0?.id){
            R.id.select_switch -> addBlock(BlockTypes.Switch)
            R.id.select_button -> addBlock(BlockTypes.Button)
            R.id.select_alarm -> addBlock(BlockTypes.Alarm)
        }
    }
    private fun addBlock(blockType: BlockTypes){
        Log.d("ParentFragment", "Parent fragment: ${parentFragment?.javaClass?.simpleName}")
        val topicDialog = AddTopicDialogFragment(blockType)
        topicDialog.setHomeFragment(homeFragment)
        topicDialog.setParentFragment(this)
        topicDialog.show(parentFragmentManager,"topic")
        dismiss()
    }
    override fun onTopicAdded(topic: String, blockType: BlockTypes) {}

}