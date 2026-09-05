package com.example.key_logging

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        // This C++ call now applies FLAG_SECURE and the BlockingAccessibilityDelegate
        nativeProtectActivity(this)

        setContent {
            var textInput by remember {
                mutableStateOf("")
            }

            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Native Accessibility Shield",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Protection is running in the C++ layer. Typekeeper is blocked.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text("Sensitive Input") },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}
