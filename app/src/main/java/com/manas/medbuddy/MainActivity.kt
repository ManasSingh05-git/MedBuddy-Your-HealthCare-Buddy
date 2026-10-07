package com.manas.medbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.manas.medbuddy.navigation.AppNavigation
import com.manas.medbuddy.ui.theme.MedBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedBuddyTheme {
                AppNavigation()
            }
        }
    }
}
