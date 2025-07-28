package com.aluma.laundry.ui.view.components

//import androidx.compose.runtime.Composable
//import androidx.compose.foundation.layout.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.draw.clip
//import androidx.compose.foundation.background
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.foundation.shape.RoundedCornerShape
//import com.patrykandpatrick.vico.core.
//import com.patrykandpatrick.vico.core.entry.FloatEntry
//import com.patrykandpatrick.vico.core.entry.rememberChartEntryModelProducer
//
//@Composable
//fun LineChartView(data: List<Float>) {
//    val entries = data.mapIndexed { index, value -> FloatEntry(index.toFloat(), value) }
//    val modelProducer = rememberChartEntryModelProducer(entries)
//
//    Chart(
//        chart = lineChart(),
//        modelProducer = modelProducer,
//        startAxis = rememberM3StartAxis(),   // Material 3 style
//        bottomAxis = rememberM3BottomAxis(),
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(180.dp)
//            .clip(RoundedCornerShape(12.dp))
//            .background(MaterialTheme.colorScheme.surfaceVariant)
//            .padding(12.dp)
//    )
//}