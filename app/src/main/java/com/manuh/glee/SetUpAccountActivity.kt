package com.manuh.glee

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_set_up_account.*

class SetUpAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up_account)

        auth = FirebaseAuth.getInstance()

        val params = window.attributes
        params.flags =
            params.flags or (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_FULLSCREEN)

        buttonFinish.setOnClickListener {
            if (fieldUsername_.text.toString() == "") {
                fieldUsername_.error = "Please enter a username to continue"
            } else {
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

        }
    }
}
