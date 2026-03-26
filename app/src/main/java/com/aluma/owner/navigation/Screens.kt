package com.aluma.owner.navigation

sealed class Screens(val route: String) {
    object HomeOwner : Screens("home_owner")
    object Login : Screens("login")
    object Machines : Screens("machines")
    object Orders : Screens("orders")
    object Services : Screens("services")
    data object Employees : Screens("employees")
    data object Attendance : Screens("attendance/{employeeId}/{employeeName}") {
        fun createRoute(employeeId: String, employeeName: String) = "attendance/$employeeId/$employeeName"
    }
}