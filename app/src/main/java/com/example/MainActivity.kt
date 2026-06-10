package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.data.database.AppDatabase
import com.example.data.repository.StudyRepository
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.StudyViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {

    private val database by lazy { 
        AppDatabase.getDatabase(
            context = this, 
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        ) 
    }
    
    private val repository by lazy { StudyRepository(database.plannerDao()) }

    private val viewModel: StudyViewModel by viewModels {
        StudyViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}
