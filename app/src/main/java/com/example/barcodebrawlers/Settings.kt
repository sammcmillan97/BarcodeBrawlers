package com.example.barcodebrawlers

import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//@Composable
//fun SettingsScreen() {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        ThemeToggleSwitch(
//            isDarkTheme = darkThemeEnabled,
//            onThemeChange = setDarkThemeEnabled
//        )
//    }
//}
//
//@Composable
//fun ThemeToggleSwitch(
//    isDarkTheme: Boolean,
//    onThemeChange: (Boolean) -> Unit
//) {
//    Row(
//        modifier = Modifier.padding(16.dp)
//    ) {
//        Text("Dark Theme")
//        Switch(
//            checked = isDarkTheme,
//            onCheckedChange = onThemeChange
//        )
//    }
//}