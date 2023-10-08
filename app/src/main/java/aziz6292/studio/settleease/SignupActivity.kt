package aziz6292.studio.settleease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.EmailAuthProvider


class SignupActivity : AppCompatActivity() {

    private lateinit var signupEmailEditText: EditText
    private lateinit var signupPasswordEditText: EditText
    private lateinit var signupButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        signupEmailEditText = findViewById(R.id.signupEmailEditText)
        signupPasswordEditText = findViewById(R.id.signupPasswordEditText)
        signupButton = findViewById(R.id.signupButton)

        signupButton.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val email = signupEmailEditText.text.toString().trim()
        val password = signupPasswordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the email is already registered
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods != null && signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                        // Email is already registered, show an error message
                        Toast.makeText(this, "Email is already registered.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Email is not registered, proceed with creating a new account
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { createUserTask ->
                                if (createUserTask.isSuccessful) {
                                    // Signup successful, write user data to the database
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId != null) {
                                        val database = FirebaseDatabase.getInstance()
                                        val usersRef: DatabaseReference = database.getReference("users")

                                        // Create user data
                                        val userData = User(email, password) // Modify this as needed

                                        // Write user data to the database under the user's UID
                                        usersRef.child(userId).setValue(userData)

                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()

                                    } else {
                                        // Handle the case where the user's UID is null
                                        Toast.makeText(this, "User's UID is null.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Signup failed, show an error message
                                    Toast.makeText(this, "Signup failed. Please try again.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // Error occurred while checking email registration status
                    Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
