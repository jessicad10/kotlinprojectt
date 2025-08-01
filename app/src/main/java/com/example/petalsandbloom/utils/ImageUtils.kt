package com.example.petalsandbloom.utils


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.content.pm.PackageManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ImageUtils(private val activity: Activity, private val registryOwner: ActivityResultRegistryOwner) {
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var writePermissionLauncher: ActivityResultLauncher<String>
    private var onImageSelectedCallback: ((Uri?) -> Unit)? = null

    fun registerLaunchers(onImageSelected: (Uri?) -> Unit) {
        Log.d("ImageUtils", "registerLaunchers called")
        onImageSelectedCallback = onImageSelected

        // Register for selecting image from gallery
        galleryLauncher = registryOwner.activityResultRegistry.register(
            "galleryLauncher", ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d("ImageUtils", "Gallery result received: ${result.resultCode}")
            val uri = result.data?.data
            if (result.resultCode == Activity.RESULT_OK && uri != null) {
                Log.d("ImageUtils", "Image selected successfully: $uri")
                onImageSelectedCallback?.invoke(uri)
            } else {
                Log.e("ImageUtils", "Image selection cancelled or failed: ${result.resultCode}")
                onImageSelectedCallback?.invoke(null)
            }
        }

        // Register permission request for reading
        permissionLauncher = registryOwner.activityResultRegistry.register(
            "permissionLauncher", ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            Log.d("ImageUtils", "Read permission result: $isGranted")
            if (isGranted) {
                openGallery()
            } else {
                Log.e("ImageUtils", "Read permission denied")
                onImageSelectedCallback?.invoke(null)
            }
        }

        // Register permission request for writing
        writePermissionLauncher = registryOwner.activityResultRegistry.register(
            "writePermissionLauncher", ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            Log.d("ImageUtils", "Write permission result: $isGranted")
            if (isGranted) {
                Log.d("ImageUtils", "Write permission granted")
            } else {
                Log.e("ImageUtils", "Write permission denied")
            }
        }
        Log.d("ImageUtils", "Launchers registered successfully")
    }

    fun launchImagePicker() {
        Log.d("ImageUtils", "launchImagePicker called at ${System.currentTimeMillis()}")
        
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        Log.d("ImageUtils", "Checking permission: $permission")
        
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ImageUtils", "Permission not granted, requesting...")
            permissionLauncher.launch(permission)
        } else {
            Log.d("ImageUtils", "Permission already granted, opening gallery...")
            openGallery()
        }
    }

    private fun openGallery() {
        Log.d("ImageUtils", "openGallery called at ${System.currentTimeMillis()}")
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            Log.d("ImageUtils", "Launching gallery intent...")
            galleryLauncher.launch(intent)
            Log.d("ImageUtils", "Gallery intent launched successfully")
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error launching gallery: ${e.message}", e)
        }
    }

    // Function to save image to phone storage
    fun saveImageToPhone(context: Context, imageUrl: String, callback: (Boolean, String) -> Unit) {
        Log.d("ImageUtils", "Attempting to save image: $imageUrl")
        
        // Check write permission
        val writePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(activity, writePermission) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ImageUtils", "Requesting write permission...")
            writePermissionLauncher.launch(writePermission)
            callback(false, "Storage permission required")
            return
        }

        try {
            // Create directory if it doesn't exist
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "PetalsAndBloom")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            // Generate unique filename
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "PetalsAndBloom_$timeStamp.jpg"
            val file = File(appDir, fileName)

            // Download and save image
            Thread {
                try {
                    val url = URL(imageUrl)
                    val inputStream: InputStream = url.openStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    
                    val outputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    Log.d("ImageUtils", "Image saved successfully: ${file.absolutePath}")
                    
                    activity.runOnUiThread {
                        callback(true, "Image saved to ${file.absolutePath}")
                    }
                } catch (e: Exception) {
                    Log.e("ImageUtils", "Error saving image: ${e.message}", e)
                    activity.runOnUiThread {
                        callback(false, "Failed to save image: ${e.message}")
                    }
                }
            }.start()

        } catch (e: Exception) {
            Log.e("ImageUtils", "Error creating file: ${e.message}", e)
            callback(false, "Failed to create file: ${e.message}")
        }
    }
}