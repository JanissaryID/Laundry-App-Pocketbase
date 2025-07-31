package com.aluma.laundry.navigation

sealed class Screens(val route: String) {
    object HomeOwner : Screens("home_owner")
    object Login : Screens("login")
    object Machines : Screens("machines")
    object Orders : Screens("orders")
    object Services : Screens("services")
}