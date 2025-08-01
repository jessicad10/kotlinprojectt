package com.example.petalsandbloom.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.petalsandbloom.R
import kotlinx.coroutines.delay


class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashBody()
        }
    }
}

@Composable
fun SplashBody() {
    val context = LocalContext.current
    val activity = context as Activity

    val sharedPreferences = context.getSharedPreferences("User", Context.MODE_PRIVATE)
    val localEmail = sharedPreferences.getString("email", "").toString()

    LaunchedEffect(Unit) {
        delay(3000) // Wait for 3 seconds
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        activity.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.petals_bloom_logo),
            contentDescription = "Petalsandbloom Logo",
            modifier = Modifier
                .width(300.dp)
                .height(300.dp)
        )
    }
}

@Preview
@Composable
fun PreviewSplash() {
    SplashBody()
}
//nice
