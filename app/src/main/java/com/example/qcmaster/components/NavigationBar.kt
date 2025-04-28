package com.example.qcmaster.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MyNavigationBar(navController: NavController) {
    // Get the current back stack entry from the NavController to track the current screen
    val navBackStackEntry = navController.currentBackStackEntryAsState()

    val items = listOf("Home", "Classes", "Students")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Class, Icons.Filled.Person)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.Class, Icons.Outlined.Person)

    // Determine which item should be selected based on the current route
    val selectedIndex = when (navBackStackEntry.value?.destination?.route) {
        "home/{profName}" -> 0
        "classes" -> 1
        "students" -> 2
        else -> 0 // Default to Home if no match
    }

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selectedIndex == index) selectedIcons[index] else unselectedIcons[index],
                        contentDescription = item
                    )
                },
                label = { Text(item) },
                selected = selectedIndex == index,
                onClick = {
                    // Navigate to the respective screen when clicked
                    when (index) {
                        0 -> navController.navigate("home/ProfName") // Update with actual profName
                        1 -> navController.navigate("classes")
                        2 -> navController.navigate("students")
                    }
                }
            )
        }
    }
}
