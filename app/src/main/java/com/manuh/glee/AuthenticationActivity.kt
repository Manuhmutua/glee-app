package com.manuh.glee

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.manuh.glee.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_authentication.*
import java.util.concurrent.TimeUnit

class AuthenticationActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    private var mUserViewModel: UserViewModel? = null

    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        val params = window.attributes
        params.flags =
            params.flags or (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        buttonStartVerification.setOnClickListener(this)
        buttonVerifyPhone.setOnClickListener(this)
        buttonResend.setOnClickListener(this)
//        signOutButton.setOnClickListener(this)


        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)

        auth = FirebaseAuth.getInstance()

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d(TAG, "onVerificationCompleted:$credential")

                verificationInProgress = false

                updateUI(STATE_VERIFY_SUCCESS, credential)

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                Log.w(TAG, "onVerificationFailed", e)
                verificationInProgress = false

                if (e is FirebaseAuthInvalidCredentialsException) {
                    fieldPhoneNumber_.error = "Invalid phone number."
                } else if (e is FirebaseTooManyRequestsException) {
                    Snackbar.make(
                        findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                updateUI(STATE_VERIFY_FAILED)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                updateUI(STATE_CODE_SENT)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)

        if (verificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(fieldPhoneNumber_.text.toString())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            callbacks
        )

        verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            callbacks,
            token
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    updateUI(STATE_SIGNIN_SUCCESS, user)
                } else {

                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        fieldVerificationCode.error = "Invalid code."
                    }
                    updateUI(STATE_SIGNIN_FAILED)

                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(
        uiState: Int,
        user: FirebaseUser? = auth.currentUser,
        cred: PhoneAuthCredential? = null
    ) {
        when (uiState) {
            STATE_INITIALIZED -> {
                enableViews(
                    textView3,
                    textView4,
                    buttonStartVerification,
                    fieldPhoneNumber
                )

                disableViews(
                    textView5,
                    textView6,
                    textView7,
                    fieldVerificationCode_,
                    buttonResend,
                    buttonVerifyPhone,
                    fieldVerificationCode,
                    progressBar
                )
                detail.text = null
            }
            STATE_CODE_SENT -> {
                enableViews(
                    textView5,
                    textView6,
                    textView7,
                    fieldVerificationCode_,
                    buttonResend,
                    buttonVerifyPhone,
                    fieldVerificationCode
                )
                disableViews(
                    textView3,
                    textView4,
                    buttonStartVerification,
                    fieldPhoneNumber,
                    progressBar
                )
                detail.setText(R.string.status_code_sent)
            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, start again
                enableViews(
                    textView3,
                    textView4,
                    buttonStartVerification,
                    fieldPhoneNumber
                )
                disableViews(
                    textView5,
                    textView6,
                    textView7,
                    fieldVerificationCode_,
                    buttonResend,
                    buttonVerifyPhone,
                    fieldVerificationCode,
                    progressBar
                )
                detail.setText(R.string.status_verification_failed)
            }
            STATE_VERIFY_SUCCESS -> {
                enableViews(
                    textView5,
                    textView6,
                    textView7,
                    fieldVerificationCode_,
                    buttonResend,
                    fieldVerificationCode,
                    buttonVerifyPhone
                )
                disableViews(
                    textView3,
                    textView4,
                    buttonStartVerification,
                    fieldPhoneNumber,
                    progressBar
                )
                detail.setText(R.string.status_verification_succeeded)
                if (cred != null) {
                    if (cred.smsCode != null) {
                        fieldVerificationCode.setText(cred.smsCode)
                    } else {
                        fieldVerificationCode.setText(R.string.instant_validation)
                    }
                }
            }
            STATE_SIGNIN_FAILED -> {
                enableViews(
                    textView3,
                    textView4,
                    buttonStartVerification,
                    fieldPhoneNumber
                )

                disableViews(
                    textView5,
                    textView6,
                    textView7,
                    fieldVerificationCode_,
                    buttonResend,
                    buttonVerifyPhone,
                    fieldVerificationCode
                )

                detail.setText(R.string.status_sign_in_failed)
                disableViews(progressBar)
            }

            STATE_SIGNIN_SUCCESS -> {
//                if (fieldVerificationCodeldIDNumber.text.toString() != "") {
//                    val u = user!!.phoneNumber?.let {
//                        User(
//                            0,
//                            fieldIDNumber.text.toString(),
//                            it,
//                            fieldFullName.text.toString(),
//                            fieldVerificationCode.text.toString(),
//                            true
//                        )
//                    }
//
//                    u?.let { mUserViewModel!!.setUser(it) }
//                }

                intent = Intent(this, SetUpAccountActivity::class.java)
                startActivity(intent)
            }
        }

    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = fieldPhoneNumber_.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            fieldPhoneNumber_.error = "Invalid phone number."
            return false
        }
        progressBar.visibility = View.VISIBLE
        return true
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.visibility = View.VISIBLE
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.visibility = View.GONE
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonStartVerification -> {
                if (!validatePhoneNumber()) {
                    return
                }

                startPhoneNumberVerification(fieldPhoneNumber_.text.toString())
            }
            R.id.buttonVerifyPhone -> {
                val code = fieldVerificationCode.text.toString()
                if (TextUtils.isEmpty(code)) {
                    fieldVerificationCode.error = "Cannot be empty."
                    return
                }
                verifyPhoneNumberWithCode(storedVerificationId, code)
            }
            R.id.buttonResend -> resendVerificationCode(
                fieldPhoneNumber_.text.toString(),
                resendToken
            )
//            R.id.signOutButton -> signOut()
        }
    }

    companion object {
        private const val TAG = "AuthenticationActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }
}

