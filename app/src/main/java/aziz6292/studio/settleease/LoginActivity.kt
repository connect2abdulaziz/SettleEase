package aziz6292.studio.settleease

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

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

        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login_btn)
        signupTextView = findViewById(R.id.signup_text)
        val back : ImageView = findViewById(R.id.back)
        back.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }

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
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val userId = user.uid

                        // Check if the user has completed the profile
                        checkProfileCompletion(userId)
                    } else {
                        Toast.makeText(this, "User is not authenticated.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Login failed. invalid email or password.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkProfileCompletion(userId: String) {
        // Assuming you have a reference to your user's profile completion status in the database
        val database = FirebaseDatabase.getInstance()
        val profileRef: DatabaseReference = database.getReference("Users").child(userId)

        profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User profile exists, navigate to the Index page
                    startActivity(Intent(this@LoginActivity, Index::class.java))
                } else {
                    // User profile does not exist, navigate to the Profile page
                    startActivity(Intent(this@LoginActivity, Profile::class.java))
                }
                finish()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error if needed
                Toast.makeText(this@LoginActivity, "Error checking profile completion.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun writeToDatabase(userId: String, userData: User) {
        val database = FirebaseDatabase.getInstance()
        val usersRef: DatabaseReference = database.getReference("users")
        usersRef.child(userId).setValue(userData)
    }
}
