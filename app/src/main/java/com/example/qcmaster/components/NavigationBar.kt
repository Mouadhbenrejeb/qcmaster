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
import com.example.qcmaster.Routes
import com.example.qcmaster.SessionManager

@Composable
fun MyNavigationBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()

    val items = listOf("Home", "Classes", "Students", "Exams")
    val routes = listOf(
        Routes.Home.route,
        Routes.Classes.route,
        Routes.Students.route,
        Routes.Exams.route
    )
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
    val selectedIndex = when (currentRoute) {
        Routes.Home.route -> 0
        Routes.Classes.route -> 1
        Routes.Students.route -> 2
        Routes.Exams.route -> 3
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
                    // Navigate using type-safe routes
                    navController.navigate(routes[index]) {
                        // Pop up to the start destination of the graph to avoid building up a large stack
                        // of destinations on the back stack as users select items
                        popUpTo(Routes.Home.route) {
                            inclusive = false
                        }
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
