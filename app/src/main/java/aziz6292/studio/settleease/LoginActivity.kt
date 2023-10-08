package aziz6292.studio.settleease

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

data class User(
    val email: String,
    val password: String
)

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupTextView: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signupTextView = findViewById(R.id.signupTextView)

        loginButton.setOnClickListener {
            loginUser()
        }

        signupTextView.setOnClickListener {
            // Display a Toast message encouraging the user to sign up
            Toast.makeText(this, "Don't have an account? Sign up now!", Toast.LENGTH_SHORT).show()

            // You can also navigate to the SignupActivity if needed
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Inside your LoginActivity, after Firebase login is successful
                if (task.isSuccessful) {
                    // Login successful, now get the user's UID
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val userId = user.uid

                        // Create user data
                        val userData = User(email, password)

                        // Write user data to the database
                        writeToDatabase(userId, userData)

                        // Proceed to the main app screen or perform other actions
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Handle the case where the user is not authenticated
                        Toast.makeText(this, "User is not authenticated.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Login failed, show an error message
                    Toast.makeText(this, "Login failed. Check your credentials.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun writeToDatabase(userId: String, userData: User) {
        val database = FirebaseDatabase.getInstance()
        val usersRef: DatabaseReference = database.getReference("users")
        usersRef.child(userId).setValue(userData)
    }
}
