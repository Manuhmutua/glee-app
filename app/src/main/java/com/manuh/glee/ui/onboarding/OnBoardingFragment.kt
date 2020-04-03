package com.manuh.glee.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.manuh.glee.AuthenticationActivity
import com.manuh.glee.MainActivity
import com.manuh.glee.R

class OnBoardingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_onboarding, container, false)

        val btnlogin = root.findViewById<Button>(R.id.buttonLogin)

        btnlogin.setOnClickListener {
            val intent = Intent(context!!.applicationContext, AuthenticationActivity::class.java)
            startActivity(intent)
        }

        return root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()

    }

}
