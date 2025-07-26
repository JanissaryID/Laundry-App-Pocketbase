package com.aluma.laundry.bluetooth

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinter() {
    private val footer = 32

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun testPrinter(context: Context, address: String?): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device = bluetoothAdapter.getRemoteDevice(address)

        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        return try {
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val outputStream = socket.outputStream

            val esc = 0x1B.toByte()
            val alignCenter = byteArrayOf(esc, 0x61, 0x01) // ESC a 1 -> center
            val newLine = "\n".repeat(5) // Spasi bawah untuk sobekan

            outputStream.write(newLine.toByteArray())
            outputStream.write(alignCenter)
            outputStream.write("Printer OK\n".toByteArray(charset("UTF-8")))
            outputStream.write(newLine.toByteArray())
            outputStream.flush()

            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printLaundryReceipt(
        context: Context,
        address: String?,
        storeName: String,
        storeAddress: String,
        storeCity: String,
        customerName: String,
        services: Pair<String, String>, // Pair(serviceName, price)
        paymentMethod: String
    ): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device = bluetoothAdapter.getRemoteDevice(address)

        val uuid = device.uuids?.firstOrNull()?.uuid
            ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        return try {
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val outputStream = socket.outputStream

            val esc = 0x1B.toByte()
            val alignLeft = byteArrayOf(esc, 0x61, 0x00)
            val alignCenter = byteArrayOf(esc, 0x61, 0x01)
            val fontNormal = byteArrayOf(esc, 0x21, 0x00)
            val fontBig = byteArrayOf(esc, 0x21, 0x30)

            outputStream.write(alignCenter)
            outputStream.write(fontBig)
            outputStream.write("$storeName\n".toByteArray())

            outputStream.write(fontNormal)
            outputStream.write("$storeAddress\n".toByteArray())
            outputStream.write("$storeCity\n\n".toByteArray())

            outputStream.write(alignLeft)
            outputStream.write("Pelanggan: $customerName\n".toByteArray())
            outputStream.write("--------------------------------\n".toByteArray())

            var total = ""
            val name = services.first.padEnd(20, ' ').take(20)
            val priceStr = "Rp ${services.second}"
            val line = "%-20s %s".format(name, priceStr)
            outputStream.write("$line\n".toByteArray())
            total = services.second

            outputStream.write("--------------------------------\n".toByteArray())

            val totalStr = "Rp $total"
            val totalLine = "%-20s %s".format("Total", totalStr)
            outputStream.write("$totalLine\n".toByteArray())

            outputStream.write("%-20s %s\n".format("Pembayaran", paymentMethod).toByteArray())

            outputStream.write("\n".toByteArray())

            outputStream.write(alignCenter)
            outputStream.write("Terima kasih sudah\n".toByteArray())
            outputStream.write("mempercayakan pakaian anda\n".toByteArray())
            outputStream.write("di ${storeName}\n".toByteArray())

            outputStream.write("\n\n\n".toByteArray())
            outputStream.flush()

            Thread.sleep(500)
            socket.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun OutputStream.writeBarcode(code: String) {
        val esc = 0x1B.toByte()
        val gs = 0x1D.toByte()

        val alignCenter = byteArrayOf(esc, 0x61, 0x01) // ESC a 1 (center)
        val barcodeSelect = byteArrayOf(gs, 0x6B, 0x49) // GS k 73 = CODE128
        val barcodeHeight = byteArrayOf(gs, 0x68, 100)  // Tinggi barcode
        val barcodeWidth = byteArrayOf(gs, 0x77, 3)     // Lebar garis (2–6)
        val showBarcodeText = byteArrayOf(gs, 0x48, 0x02) // Tampilkan text di bawah barcode

        write(alignCenter)
        write(barcodeHeight)
        write(barcodeWidth)
        write(showBarcodeText)

        write(barcodeSelect)
        write(code.length)
        write(code.toByteArray())
        write("\n".toByteArray())

//        write(code.toByteArray()) // Kode di bawah barcode
        write("\n\n".toByteArray())
    }

    private fun OutputStream.writeBarcodeOrder(code: String) {
        val esc = 0x1B.toByte()
        val gs = 0x1D.toByte()

        val alignCenter = byteArrayOf(esc, 0x61, 0x01) // ESC a 1 (center)
        val barcodeType = byteArrayOf(gs, 0x6B, 0x49) // GS k 73 = CODE128
        val barcodeHeight = byteArrayOf(gs, 0x68, 100)  // Barcode height
        val barcodeWidth = byteArrayOf(gs, 0x77, 2)     // Line width (2-6)
        val showBarcodeText = byteArrayOf(gs, 0x48, 0x02) // Show text below

        write(alignCenter)
        write(barcodeHeight)
        write(barcodeWidth)
        write(showBarcodeText)

        write(barcodeType)
        write(byteArrayOf(code.length.toByte()))
        write(code.toByteArray())   // Data barcode

        write("\n\n".toByteArray())
    }
}

