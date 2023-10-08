package aziz6292.studio.settleease

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ServicesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        val servicesText: TextView = findViewById(R.id.servicesText)

        // Set the service description text from string resources
        servicesText.text = getString(R.string.services_description)
    }
}
