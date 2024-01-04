package com.example.se

import android.graphics.Bitmap
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
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView

class Index : AppCompatActivity() {

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressedMethod()
        }
    }

    private lateinit var drawerLayout: DrawerLayout
    private fun onBackPressedMethod() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)

      // Nav Header
        val receivedImageResource = intent.getParcelableExtra<Bitmap>("imageResource")
        val receivedName = intent.getStringExtra("Name")
        val receivedOccupation = intent.getStringExtra("Occupation")

        // Update UI with the received data
        val pic: CircleImageView = findViewById(R.id.pic)
        val name: TextView = findViewById(R.id.name)
        val occup: TextView = findViewById(R.id.occupation)

        pic.setImageBitmap(receivedImageResource)
        name.text = receivedName
        occup.text = receivedOccupation



        // Nav item
        drawerLayout = findViewById(R.id.Drawer_layout)

        val navView: NavigationView = findViewById(R.id.navigation_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        // _______Set toolbar _____:
        setSupportActionBar(toolbar)

        // _______________Navigation Drawer_______________:

        // Navigation drawer toggle open-> close , close->open drawerr ko
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
                    true
                }

                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    supportActionBar?.title = "Profile"
                    true
                }

                R.id.services -> {
                    replaceFragment(ServicesFragment())
                    supportActionBar?.title = "Services"
                    true
                }

                R.id.setting -> {
                    replaceFragment(SettingFragment())
                    supportActionBar?.title = "Setting"
                    true
                }

                R.id.logout -> {
                    Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                // Add more cases if needed.
                else -> false
            }
        }


        // Add the onBackPressedCallback to the OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // _____________bottom Navigation __________________:
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav)

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    replaceFragment(HomeFragment())
                    supportActionBar?.title = "Home"
                    Log.d("BottomNav", "Home selected")
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
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

}