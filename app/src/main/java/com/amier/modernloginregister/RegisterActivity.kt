package com.amier.modernloginregister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*
import java.util.concurrent.Executor
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btnLogRegister.setOnClickListener {
            onBackPressed()
        }
        mDatabase = FirebaseDatabase.getInstance().reference

        val btnRegister: Button = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
//            startActivity(Intent(this,RegisterActivity::class.java))
//            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)

            SafetyNet.getClient(this).verifyWithRecaptcha("6LeVQysfAAAAAKPzGDPYL8sgcrfmIDusIT344TlD")
                .addOnSuccessListener { response ->
                    // Indicates communication with reCAPTCHA service was
                    // successful.
                    val userResponseToken = response.tokenResult
                    if (!userResponseToken!!.isEmpty()) {
                        register()
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

            auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if(currentUser != null){
//            reload();
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
    }

    private fun register() {
        Log.d("Registering", "RegStart")
        val email:TextInputEditText = findViewById(R.id.reg_email)
        val pass:TextInputEditText = findViewById(R.id.reg_password)
        val name:TextInputEditText = findViewById(R.id.reg_name)
        val answer:TextInputEditText = findViewById(R.id.reg_answer)
        val question:Spinner = findViewById(R.id.spinner)
        auth.createUserWithEmailAndPassword(email.text.toString(), pass.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "signInWithEmail:success")
                    val user = auth.currentUser
                    val uid:String = user?.uid ?: "NULL";
                    Log.d("TAG", "signInWithEmail:success")
                    mDatabase.child("$uid/question").setValue(question.selectedItem);
                    mDatabase.child("$uid/name").setValue(name.text.toString());
                    mDatabase.child("$uid/answer").setValue(answer.text.toString());

                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

}
