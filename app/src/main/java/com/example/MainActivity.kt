package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.AppContent
import com.example.ui.HospitalViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: HospitalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initial configuration checks (Setup vs Login)
        viewModel.checkAppConfig()

        setContent {
            AppContent(viewModel = viewModel)
        }
    }
}
