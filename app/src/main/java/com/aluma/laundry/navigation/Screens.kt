package com.aluma.laundry.navigation

sealed class Screens(val route: String) {
    object Home : Screens("home")
    object HomeOwner : Screens("home_owner")
    object Login : Screens("login")
    object ChoseStore : Screens("chose_store")
    object Settings : Screens("settings")
    object Machines : Screens("machines")
    object Orders : Screens("orders")
    object Store : Screens("store")
    object Services : Screens("services")
}