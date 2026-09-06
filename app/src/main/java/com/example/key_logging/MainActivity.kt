package com.example.key_logging

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    // Native method in C++ to apply multi-layer protection
    private external fun nativeProtectActivity(activity: ComponentActivity)

    companion object {
        init {
            System.loadLibrary("key_logging")
        }
        private const val TAG = "ACCESSIBILITY_TEST"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "Activity created - applying native protection")
        nativeProtectActivity(this)

        setContent {
            var textInput by remember { mutableStateOf("") }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF6366F1), // Modern Indigo Accent
                    onSurface = Color.White,
                    onSurfaceVariant = Color(0xFF9CA3AF)
                )
            ) {
                // Rich dark gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF090D16)
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Sleek Glassmorphism Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.55f),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.02f)
                                )
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Security Shield",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "C++ Protection & Typekeeper Block Active",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Modern Input Field
                            TextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = { Text("Enter Sensitive Data") },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.75f),
                                    unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.45f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedLabelColor = Color(0xFF6366F1),
                                    cursorColor = Color(0xFF6366F1)
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action Button
                            ElevatedButton(
                                onClick = {
                                    val message = if (textInput.isNotBlank()) {
                                        "Submitted: $textInput"
                                    } else {
                                        "Please enter some text"
                                    }
                                    Toast.makeText(
                                        this@MainActivity,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = Color(0xFF6578FD),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Submit",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}