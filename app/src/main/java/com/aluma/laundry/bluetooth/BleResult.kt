package com.aluma.laundry.bluetooth

/**
 * Sealed class representing the result of a BLE operation with ESP32.
 *
 * Response format from ESP32:
 * - Status: "<stage>|<id_8char>"  e.g. "3|6306db9d"
 * - Version: "ver|<version>"     e.g. "ver|v1.4"
 */
sealed class BleResult {
    data class Status(val stage: Int, val transactionId8: String) : BleResult()
    data class Version(val version: String) : BleResult()
    data class Error(val message: String) : BleResult()

    companion object {
        fun parse(response: String): BleResult {
            val cleanResponse = response.replace("\u0000", "").trim()
            val parts = cleanResponse.split("|")
            return when {
                parts.size >= 2 && parts[0] == "ver" ->
                    Version(parts[1])

                parts.size >= 2 && parts[0].toIntOrNull() != null ->
                    Status(stage = parts[0].toInt(), transactionId8 = parts[1])

                else ->
                    Error("Unknown response: $cleanResponse")
            }
        }
    }
}
