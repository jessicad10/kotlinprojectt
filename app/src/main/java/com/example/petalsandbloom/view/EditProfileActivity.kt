
package com.example.petalsandbloom.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.petalsandbloom.R
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.ktx.Firebase

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EditProfileScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val currentUser = Firebase.auth.currentUser
    var name by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var showPasswordDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            if (currentUser?.photoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentUser.photoUrl)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.placeholder),
                    contentDescription = "Profile Placeholder",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    user.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            val userId = user.uid
                            val userMap = mapOf(
                                "firstName" to name,
                                "email" to email,
                                "image" to (user.photoUrl?.toString() ?: "")
                            )
                            FirebaseDatabase.getInstance().getReference("users").child(userId)
                                .updateChildren(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to update user data", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Profile update failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                focusManager.clearFocus()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Save Changes")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showPasswordDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Change Password")
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onChange = { current, new ->
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && user.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, current)
                    user.reauthenticate(credential).addOnSuccessListener {
                        user.updatePassword(new).addOnSuccessListener {
                            Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
                showPasswordDialog = false
            }
        )
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChange: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                    onChange(currentPassword, newPassword)
                }
            }) {
                Text("Change")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

