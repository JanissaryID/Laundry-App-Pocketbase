package com.aluma.owner.utils

import android.os.Environment
import android.util.Log
import com.aluma.owner.data.logmachine.model.LogMachineRemote
import com.aluma.owner.data.order.model.OrderRemote
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExcelPOIViewModel : ViewModel() {
    private val _stateExcel = MutableStateFlow<Boolean>(false)
    val stateExcel: StateFlow<Boolean> = _stateExcel

    private suspend fun exportToExcel(
        date: String,
        storeName: String,
        storeAddress: String,
        orderList: List<OrderRemote>,
        logList: List<LogMachineRemote>
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheetTransaksi = workbook.createSheet("Transaksi")
            val sheetMesin = workbook.createSheet("Mesin")

            val dateNow = date.ifEmpty {
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            }

            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
                maximumFractionDigits = 0
            }

            // === Styles ===
            val headerStyle = workbook.createCellStyle().apply {
                setFont(workbook.createFont().apply {
                    bold = true
                    fontHeightInPoints = 12
                })
                alignment = HorizontalAlignment.CENTER
                borderStyle(this)
            }

            val normalStyle = workbook.createCellStyle().apply {
                alignment = HorizontalAlignment.LEFT
                borderStyle(this)
            }

            val currencyStyle = workbook.createCellStyle().apply {
                alignment = HorizontalAlignment.LEFT
                borderStyle(this)
            }

            val numberCenterStyle = workbook.createCellStyle().apply {
                alignment = HorizontalAlignment.CENTER
                borderStyle(this)
            }

            // === SHEET TRANSAKSI ===
            var rowIdx = 0
            sheetTransaksi.createRow(rowIdx++).apply {
                createCell(0).setCellValue("Toko")
                createCell(1).setCellValue(storeName)
            }
            sheetTransaksi.createRow(rowIdx++).apply {
                createCell(0).setCellValue("Alamat")
                createCell(1).setCellValue(storeAddress)
            }
            sheetTransaksi.createRow(rowIdx++).apply {
                createCell(0).setCellValue("Tanggal")
                createCell(1).setCellValue(dateNow)
            }

            rowIdx++

            val headers = listOf("No", "Nama Layanan", "Pelanggan", "Ukuran", "Tipe Mesin", "Pembayaran", "Harga")
            val headerRow = sheetTransaksi.createRow(rowIdx++)
            headers.forEachIndexed { idx, title ->
                headerRow.createCell(idx).apply {
                    setCellValue(title)
                    cellStyle = headerStyle
                }
            }

            var totalCash = 0
            var totalQris = 0

            orderList.forEachIndexed { index, order ->
                val row = sheetTransaksi.createRow(rowIdx++)
                row.createCell(0).apply {
                    setCellValue((index + 1).toDouble())
                    cellStyle = numberCenterStyle
                }
                row.createCell(1).apply {
                    setCellValue(order.serviceName ?: "")
                    cellStyle = normalStyle
                }
                row.createCell(2).apply {
                    setCellValue(order.customerName ?: "")
                    cellStyle = normalStyle
                }
                row.createCell(3).apply {
                    setCellValue(if (order.sizeMachine) "Besar" else "Kecil")
                    cellStyle = normalStyle
                }
                row.createCell(4).apply {
                    setCellValue(
                        when (order.typeMachineService) {
                            0 -> "Washer"
                            1 -> "Dryer"
                            else -> "Unknown"
                        }
                    )
                    cellStyle = normalStyle
                }
                row.createCell(5).apply {
                    setCellValue(order.typePayment ?: "-")
                    cellStyle = normalStyle
                }
                row.createCell(6).apply {
                    val priceInt = order.price?.toIntOrNull() ?: 0
                    setCellValue(numberFormat.format(priceInt))
                    cellStyle = currencyStyle
                    if (order.typePayment?.lowercase() == "qris") totalQris += priceInt else totalCash += priceInt
                }
            }

            val totalRow = sheetTransaksi.createRow(rowIdx++)
            totalRow.createCell(5).apply {
                setCellValue("Total")
                cellStyle = headerStyle
            }
            totalRow.createCell(6).apply {
                setCellValue(numberFormat.format(totalCash + totalQris))
                cellStyle = currencyStyle
            }

            rowIdx++
            sheetTransaksi.createRow(rowIdx++).apply {
                createCell(1).setCellValue("Total Cash")
                createCell(2).setCellValue(": ${numberFormat.format(totalCash)}")
            }
            sheetTransaksi.createRow(rowIdx++).apply {
                createCell(1).setCellValue("Total Qris")
                createCell(2).setCellValue(": ${numberFormat.format(totalQris)}")
            }

            // === SHEET MESIN ===
            var rowMesin = 0
            sheetMesin.createRow(rowMesin++).apply {
                createCell(0).setCellValue("Toko")
                createCell(1).setCellValue(storeName)
            }
            sheetMesin.createRow(rowMesin++).apply {
                createCell(0).setCellValue("Alamat")
                createCell(1).setCellValue(storeAddress)
            }
            sheetMesin.createRow(rowMesin++).apply {
                createCell(0).setCellValue("Tanggal")
                createCell(1).setCellValue(dateNow)
            }

            rowMesin++

            val mesinHeader = listOf("No", "No Mesin", "Tipe Mesin", "Ukuran")
            val mesinHeaderRow = sheetMesin.createRow(rowMesin++)
            mesinHeader.forEachIndexed { idx, title ->
                mesinHeaderRow.createCell(idx).apply {
                    setCellValue(title)
                    cellStyle = headerStyle
                }
            }

            var washerKecil = 0
            var washerBesar = 0
            var dryerKecil = 0
            var dryerBesar = 0

            logList.forEachIndexed { index, log ->
                val row = sheetMesin.createRow(rowMesin++)
                row.createCell(0).apply {
                    setCellValue((index + 1).toDouble())
                    cellStyle = numberCenterStyle
                }
                row.createCell(1).apply {
                    setCellValue(log.numberMachine.toString())
                    cellStyle = numberCenterStyle
                }
                row.createCell(2).apply {
                    setCellValue(if (log.typeMachine) "Dryer" else "Washer")
                    cellStyle = normalStyle
                }
                row.createCell(3).apply {
                    setCellValue(if (log.sizeMachine) "Besar" else "Kecil")
                    cellStyle = normalStyle
                }

                when {
                    !log.typeMachine && !log.sizeMachine -> washerKecil++
                    !log.typeMachine && log.sizeMachine -> washerBesar++
                    log.typeMachine && !log.sizeMachine -> dryerKecil++
                    else -> dryerBesar++
                }
            }

            rowMesin++
            sheetMesin.createRow(rowMesin++).apply {
                createCell(1).setCellValue("Washer Kecil")
                createCell(2).setCellValue(": $washerKecil")
            }
            sheetMesin.createRow(rowMesin++).apply {
                createCell(1).setCellValue("Washer Besar")
                createCell(2).setCellValue(": $washerBesar")
            }
            sheetMesin.createRow(rowMesin++).apply {
                createCell(1).setCellValue("Dryer Kecil")
                createCell(2).setCellValue(": $dryerKecil")
            }
            sheetMesin.createRow(rowMesin++).apply {
                createCell(1).setCellValue("Dryer Besar")
                createCell(2).setCellValue(": $dryerBesar")
            }

            listOf(sheetTransaksi, sheetMesin).forEach { sheet ->
                for (i in 0..6) {
                    sheet.setColumnWidth(i, 20 * 256)
                }
            }

            val fileName = "Laporan_${dateNow}.xlsx"
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )
            val fos = FileOutputStream(file)
            workbook.write(fos)
            fos.close()
            workbook.close()

            Log.d("excel", "Berhasil simpan di ${file.absolutePath}")
            _stateExcel.value = true
            true
        } catch (e: Exception) {
            Log.e("excel", "Gagal simpan Excel: ${e.message}")
            _stateExcel.value = false
            false
        }
    }

    private fun borderStyle(style: CellStyle) {
        style.borderTop = BorderStyle.THIN
        style.borderBottom = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
    }

    fun createExcelReport(
        orderList: List<OrderRemote>,
        logList: List<LogMachineRemote>,
        storeAddress: String,
        storeName: String,
        date: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = try {
                val result = exportToExcel(date = date, storeName = storeName, storeAddress = storeAddress, logList = logList, orderList = orderList)
                result
            } catch (e: Exception) {
                Log.e("Excel", "Gagal ekspor Excel: ${e.message}")
                false
            }

            withContext(Dispatchers.Main) {
                _stateExcel.value = success
                onResult(success)
            }
        }
    }

}
