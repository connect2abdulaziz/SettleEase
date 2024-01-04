package com.example.se

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
import com.example.se.databinding.ActivityProfileBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID


class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference
    private lateinit var storageRef: FirebaseStorage
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  Inflate binding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        //  Set content view to binding.root
        setContentView(binding.root)

        // Firebase instance
        auth = FirebaseAuth.getInstance()
        // Current user
        val uid = auth.currentUser?.uid
        dbReference = FirebaseDatabase.getInstance().getReference("Users")

        binding.camera.setOnClickListener {
            showCustomDialogBox()
        }

        binding.join.setBackgroundColor(ContextCompat.getColor(this, R.color.purple))

        binding.join.setOnClickListener {
            val image=imageUri.toString()
            val name=binding.name.text.toString()
            val phone=binding.phone.text.toString()
            val email=binding.email.text.toString()
            val address=binding.address.text.toString()
            val cnic=binding.cnic.text.toString()
            val city=binding.city.text.toString()
            val country=binding.country.text.toString()
            val postAddress=binding.postal.text.toString()
            val lang=binding.lang.text.toString()
            val dob=binding.dob.text.toString()
            val occupation=binding.occupation.text.toString()

            if(name.isNotEmpty()&&phone.isNotEmpty()&&email.isNotEmpty()&&address.isNotEmpty()
                &&cnic.isNotEmpty()&& city.isNotEmpty()&&country.isNotEmpty()&&postAddress.isNotEmpty()
                &&lang.isNotEmpty() &&dob.isNotEmpty()&& occupation.isNotEmpty())
            {
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

                        } else {
                            Toast.makeText(
                                this@Profile,
                                "Fail to save user Profile.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }


                val image: CircleImageView = findViewById(R.id.picture)
                val drawable = image.drawable
                val imageBitmap = (drawable as? BitmapDrawable)?.bitmap // Convert drawable to bitmap

                val iNext = Intent(this, Index::class.java)
                iNext.putExtra("imageResource", imageBitmap)
                iNext.putExtra("Name", name)
                iNext.putExtra("Occupation", occupation)
                startActivity(iNext)
                finish()

            }
            else
                Toast.makeText(
                    this@Profile,
                    "Fill All Fields.",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            1 -> {
                if (resultCode == RESULT_OK) {
                    // The image data is in the extras // data store in bitmap form when capture from camera
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    // You can use the imageBitmap as needed
                    // For now, let's save it to a temporary file
                    val tempFile = createTempFile("profile_image", ".jpg", cacheDir)
                    tempFile.outputStream().use {
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }

                    imageUri = Uri.fromFile(tempFile)
                    binding.picture.setImageURI(imageUri)
                    uploadToFirebase(imageUri)
                }
            }
            2 -> {
                if (resultCode == RESULT_OK && data != null) {
                    imageUri = data.data!!
                    binding.picture.setImageURI(imageUri)
                    uploadToFirebase(imageUri)
                }
            }
        }
    }


    private fun uploadToFirebase(uri: Uri) {
        // progress dialoag
        if (uri != null) {
            val progDialog = ProgressDialog(this)
            progDialog.setTitle("Uploading Image.....")
            progDialog.setMessage("Progressing.....")
            progDialog.show()

            val storageRef =
                FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString())

            storageRef.putFile(uri).addOnSuccessListener {
                progDialog.dismiss()
                Toast.makeText(applicationContext, "File Upload successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                progDialog.dismiss()
                Toast.makeText(applicationContext, "File Upload Fail...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
