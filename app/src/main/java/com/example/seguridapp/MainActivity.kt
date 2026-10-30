package com.example.seguridapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.seguridapp.ui.navigation.SeguridAppNavigation
import com.example.seguridapp.ui.theme.SeguridappTheme
import com.example.seguridapp.viewmodel.AppViewModelProvider
import com.example.seguridapp.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeguridappTheme {
                val authVm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
                SeguridAppNavigation(authVm)
            }
        }
    }
}
