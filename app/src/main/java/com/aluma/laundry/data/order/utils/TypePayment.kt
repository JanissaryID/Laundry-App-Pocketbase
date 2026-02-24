package com.aluma.laundry.data.order.utils

import androidx.annotation.StringRes
import com.aluma.laundry.R

enum class TypePayment(@StringRes val labelRes: Int) {
    TUNAI(R.string.payment_cash),
    QRIS(R.string.payment_qris);

    companion object {
        // fromLabel removed as it relied on hardcoded strings
    }
}
