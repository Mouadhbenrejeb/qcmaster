package com.example.qcmaster.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.qcmaster.SessionManager

@Composable
fun MyNavigationBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()

    val items = listOf("Home", "Classes", "Students", "Exams")
    val selectedIcons = listOf(
        Icons.Filled.Home,
        Icons.Filled.Class,
        Icons.Filled.Person,
        Icons.Filled.Edit
    )
    val unselectedIcons = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.Class,
        Icons.Outlined.Person,
        Icons.Outlined.Edit
    )

    val currentRoute = navBackStackEntry.value?.destination?.route
    val selectedIndex = when {
        currentRoute?.startsWith("home") == true -> 0
        currentRoute == "classes" -> 1
        currentRoute == "students" -> 2
        currentRoute == "exams" -> 3
        else -> 0
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
                    when (index) {
                        0 -> navController.navigate("home/${SessionManager.profName}/${SessionManager.profEmail}")
                        1 -> navController.navigate("classes")
                        2 -> navController.navigate("students")
                        3 -> navController.navigate("exams")
                    }
                }
            )
        }
    }
}
