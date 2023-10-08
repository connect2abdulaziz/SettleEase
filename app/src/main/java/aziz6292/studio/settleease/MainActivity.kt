package aziz6292.studio.settleease

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val joinButton: Button = findViewById(R.id.joinButton)
        val servicesButton: Button = findViewById(R.id.servicesButton)

        joinButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        servicesButton.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }
    }
}
