@file:Suppress("DEPRECATION")

package aziz6292.studio.settleease

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import aziz6292.studio.settleease.databinding.ActivityProfileBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.Calendar

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUri = Uri.EMPTY // or use a default Uri

        binding.camera.setOnClickListener {
            showCustomDialogBox()
        }

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        dbReference = FirebaseDatabase.getInstance().getReference("Users")

        binding.camera.setOnClickListener {
            showCustomDialogBox()
        }

        binding.join.setBackgroundColor(ContextCompat.getColor(this, R.color.purple))

        binding.join.setOnClickListener {
            imageUri.toString()
            val name = binding.name.text.toString()
            val phone = binding.phone.text.toString()
            val email = binding.email.text.toString()
            val address = binding.address.text.toString()
            val cnic = binding.cnic.text.toString()
            val city = binding.city.text.toString()
            val country = binding.country.text.toString()
            val postAddress = binding.postal.text.toString()
            val lang = binding.lang.text.toString()
            val dob = binding.dobEditText.text.toString()
            val occupation = binding.occupation.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty() && address.isNotEmpty()
                && cnic.isNotEmpty() && city.isNotEmpty() && country.isNotEmpty() && postAddress.isNotEmpty()
                && lang.isNotEmpty() && dob.toString().isNotEmpty() && occupation.isNotEmpty()
            ) {
                val user = UserProfile(
                    imageUri.toString(),
                    name,
                    phone,
                    email,
                    address,
                    cnic,
                    city,
                    country,
                    postAddress,
                    lang,
                    dob,
                    occupation
                )

                if (uid != null) {
                    dbReference.child(uid).setValue(user).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@Profile,
                                "Successfully save user Profile.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Upload image to Firebase Storage and save URL to database
                            uploadToFirebase(uid)
                        } else {
                            Toast.makeText(
                                this@Profile,
                                "Fail to save user Profile.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@Profile,
                        "User ID is null.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else
                Toast.makeText(
                    this@Profile,
                    "Fill All Fields.",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun showCustomDialogBox() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_alertdialog)

        val yesBtn = dialog.findViewById(R.id.camerabtn) as FloatingActionButton
        yesBtn.setOnClickListener {
            // Launch the camera to take a photo
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, 1)
            }
        }

        val noBtn = dialog.findViewById(R.id.gallery) as FloatingActionButton
        noBtn.setOnClickListener {
            val iNext = Intent(Intent.ACTION_GET_CONTENT)
            iNext.type = "image/*"
            startActivityForResult(iNext, 2)
            dialog.dismiss()
        }

        val cancel = dialog.findViewById(R.id.cancel) as TextView
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            1 -> {
                if (resultCode == RESULT_OK) {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    val tempFile = createTempFile("profile_image", ".jpg", cacheDir)
                    tempFile.outputStream().use {
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }

                    imageUri = Uri.fromFile(tempFile)
                    binding.picture.setImageURI(imageUri)
                    // Note: Don't call uploadToFirebase here, it will be called after saving user profile
                }
            }
            2 -> {
                if (resultCode == RESULT_OK && data != null) {
                    imageUri = data.data!!
                    binding.picture.setImageURI(imageUri)
                    // Note: Don't call uploadToFirebase here, it will be called after saving user profile
                }
            }
        }
    }

    private fun uploadToFirebase(uid: String) {
        val storageRef =
            FirebaseStorage.getInstance().reference.child("images/$uid.jpg")

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Saving Data.....")
        progressDialog.setMessage("Progressing.....")
        progressDialog.show()

        // Convert the imageUri to a byte array
        val bitmap = (binding.picture.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    applicationContext,
                    "File Upload successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Save the image URL to the user's profile in the database
                saveImageUrlToDatabase(storageRef, uid)

                startActivity(Intent(this, Index::class.java))
                finish()

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "File Upload Fail...", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToDatabase(imageRef: StorageReference, uid: String) {
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            // Update the user's profile in the database with the image URL
            dbReference.child(uid).child("profileImageUrl").setValue(uri.toString())
        }
    }
}
