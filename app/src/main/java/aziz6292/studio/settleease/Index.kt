@file:Suppress("DEPRECATION")

package aziz6292.studio.settleease

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import aziz6292.studio.settleease.databinding.ActivityIndexBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.FirebaseApp
import java.io.File
import java.io.IOException

class Index : AppCompatActivity() {

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressedMethod()
        }
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityIndexBinding
    private lateinit var pictureImageView: CircleImageView
    private lateinit var nameTextView: TextView
    private lateinit var occupationTextView: TextView
    private lateinit var storageReference: StorageReference
    private lateinit var progressDialog: ProgressDialog
    private lateinit var databaseReference: DatabaseReference

    private fun onBackPressedMethod() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        drawerLayout = binding.DrawerLayout
        val navView: NavigationView = findViewById(R.id.navigation_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.Open_Drawer,
            R.string.Close_Drawer
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    supportActionBar?.title = "Home"
                    closeDrawer()
                    true
                }
                R.id.bottom_chat ->{
                    replaceFragment(ChatFragment())
                    supportActionBar?.title="Chat"
                    closeDrawer()
                    true
                }

                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    supportActionBar?.title = "Profile"
                    closeDrawer()
                    true
                }
                R.id.services -> {
                    replaceFragment(ServicesFragment())
                    supportActionBar?.title = "Services"
                    closeDrawer()
                    true
                }
                R.id.setting -> {
                    replaceFragment(SettingFragment())
                    supportActionBar?.title = "Setting"
                    closeDrawer()
                    true
                }
                R.id.logout -> {
                    Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()

                    // Redirect the user to the login or splash screen
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav)

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    replaceFragment(HomeFragment())
                    supportActionBar?.title = "Home"
                    Log.d("BottomNav", "Home selected")
                    true
                }
                R.id.bottom_chat -> {
                    replaceFragment(ChatFragment())
                    supportActionBar?.title = "Chat"
                    Log.d("BottomNav", "Chat selected")
                    true
                }
                R.id.bottom_profile -> {
                    replaceFragment(ProfileFragment())
                    supportActionBar?.title = "Profile"
                    Log.d("BottomNav", "Profile selected")
                    true
                }
                R.id.bottom_services -> {
                    replaceFragment(ServicesFragment())
                    supportActionBar?.title = "Services"
                    Log.d("BottomNav", "Services selected")
                    true
                }
                else -> false
            }
        }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment()).commit()
            supportActionBar?.title = "Home"
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading Data...")
        progressDialog.setCancelable(false)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid

            pictureImageView = binding.navigationView.getHeaderView(0).findViewById(R.id.picture)
            nameTextView = binding.navigationView.getHeaderView(0).findViewById(R.id.name)
            occupationTextView = binding.navigationView.getHeaderView(0).findViewById(R.id.occupation)

            storageReference = FirebaseStorage.getInstance().getReference("images/$uid.jpg")



            databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            val profileRef: DatabaseReference = databaseReference.child(uid)

            profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userProfile = dataSnapshot.getValue(UserProfile::class.java)

                        if (userProfile != null) {
                            nameTextView.text = userProfile.userName
                            occupationTextView.text = userProfile.occupation

                            val imageReference: StorageReference = storageReference

                            try {
                                val localFile = File.createTempFile("tempfile", ".jpg")

                                progressDialog.show()

                                imageReference.getFile(localFile)
                                    .addOnSuccessListener {
                                        progressDialog.dismiss()

                                        val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                        pictureImageView.setImageBitmap(bitmap)
                                    }
                                    .addOnFailureListener {
                                        progressDialog.dismiss()
                                        // Handle the failure if needed
                                    }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@Index, "Error fetching user data.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this@Index, "No authenticated user.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }
    private fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }
}
