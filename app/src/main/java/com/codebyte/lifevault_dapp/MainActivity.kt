package com.codebyte.lifevault_dapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codebyte.lifevault_dapp.ui.navigation.Navigation
import com.codebyte.lifevault_dapp.ui.theme.LifeVaultDappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LifeVaultDappTheme() {
                val viewModel: MainViewModel = viewModel()
                Navigation(viewModel)
            }
        }
    }
}