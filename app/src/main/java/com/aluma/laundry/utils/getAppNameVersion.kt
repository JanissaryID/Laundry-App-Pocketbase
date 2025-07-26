package com.aluma.laundry.utils

import android.content.Context

fun Context.getAppInfo(): Pair<String, String> {
    val packageManager = this.packageManager
    val packageName = this.packageName
    val appInfo = packageManager.getApplicationInfo(packageName, 0)
    val appName = packageManager.getApplicationLabel(appInfo).toString()

    val version = packageManager.getPackageInfo(packageName, 0).versionName

    return appName to (version ?: "Unknown")
}
