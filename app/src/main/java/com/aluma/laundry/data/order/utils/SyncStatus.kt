package com.aluma.laundry.data.order.utils

enum class SyncStatus(val value: String) {
    PENDING("PENDING"),     // belum dikirim ke server
    SYNCED("SYNCED"),       // sudah dikirim
    FAILED("FAILED");        // gagal dikirim, akan dicoba ulang

    override fun toString(): String = value

    companion object {
        fun from(value: String): SyncStatus {
            return entries.firstOrNull { it.value == value }
                ?: PENDING // default fallback
        }
    }
}