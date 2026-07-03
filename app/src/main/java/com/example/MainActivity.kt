package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.LabRepository
import com.example.ui.LabViewModel
import com.example.ui.LabViewModelFactory
import com.example.ui.MainAppLayout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Offline Room SQLite Database and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = LabRepository(database.labDao())

        // Build View Model via custom provider factory
        val factory = LabViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[LabViewModel::class.java]

        setContent {
            // Render primary responsive Material 3 layout
            MainAppLayout(viewModel = viewModel)
        }
    }
}
