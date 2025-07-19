package com.aluma.laundry.navigation

sealed class Screens(val route: String) {
    object Home : Screens("home")
    object Login : Screens("login")
    object ChoseStore : Screens("chose_store")
}