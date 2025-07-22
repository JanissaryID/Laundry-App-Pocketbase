package com.aluma.laundry.data.api.order.model

enum class TypePayment(val label: String) {
    TUNAI("Tunai"),
    QRIS("QRIS");

    companion object {
        fun fromLabel(label: String): TypePayment? {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
        }
    }
}
