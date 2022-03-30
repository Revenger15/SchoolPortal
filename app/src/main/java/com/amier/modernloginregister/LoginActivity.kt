package com.amier.modernloginregister

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.common.api.CommonStatusCodes

import com.google.android.gms.common.api.ApiException

import androidx.appcompat.app.AlertDialog

import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.safetynet.SafetyNetApi.RecaptchaTokenResponse

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import java.util.concurrent.Executor


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var email: TextInputEditText
    lateinit var password: TextInputEditText
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btnRegLogin.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }
        mDatabase = FirebaseDatabase.getInstance().reference

        var btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {

            SafetyNet.getClient(this).verifyWithRecaptcha("6LeVQysfAAAAAKPzGDPYL8sgcrfmIDusIT344TlD")
                .addOnSuccessListener { response ->
                    // Indicates communication with reCAPTCHA service was
                    // successful.
                    val userResponseToken = response.tokenResult
                    if (!userResponseToken!!.isEmpty()) {
                        auth.signInWithEmailAndPassword(
                            email.text.toString(),
                            password.text.toString()
                        )
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("TAG", "signInWithEmail:success")
                                    val user = auth.currentUser
                                    val uid: String = user?.uid ?: "NULL";

                                    showdialog(uid)
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                                    Toast.makeText(
                                        baseContext, "Authentication failed.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    if (e is ApiException) {
                        // An error occurred when communicating with the
                        // reCAPTCHA service. Refer to the status code to
                        // handle the error appropriately.
                        val apiException = e as ApiException
                        val statusCode = apiException.statusCode
                        Log.d(
                            "TAG", "Error: " + CommonStatusCodes
                                .getStatusCodeString(statusCode)
                        )
                    } else {
                        // A different, unknown type of error occurred.
                        Log.d("TAG", "Error: " + e.message)
                    }
                }
        }

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if(currentUser != null){
//            reload();
        }

        email = findViewById(R.id.in_email)
        password = findViewById(R.id.in_password)
    }

    fun showdialog(uid: String) {
        mDatabase.child(uid).get().addOnSuccessListener {

            val answer: String = it.child("answer").value.toString()
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("2FA Question\n"+it.child("question").value.toString())

// Set up the input
            val input = EditText(this)
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setHint("Enter Text")
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

// Set up the buttons
            builder.setPositiveButton("OK") { dialog, which ->
                // Here you get get input text from the Edittext
                var m_Text = input.text.toString()
                if(m_Text.equals(answer)) {
                    dialog.dismiss()
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
                } else {
                    dialog.dismiss()
                    Toast.makeText(this, "Answer is incorrect", Toast.LENGTH_LONG).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel(); auth.signOut() }

            builder.show()
        }
    }
}
