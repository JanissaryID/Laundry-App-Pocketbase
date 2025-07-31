package com.aluma.laundry.data.income.remote

import com.aluma.laundry.data.income.model.IncomeRemote

interface IncomeRemoteRepository {
    suspend fun createIncome(income: IncomeRemote)
    suspend fun updateIncome(incomeId: String, income: String)
    suspend fun getIncomeByDate(storeId: String, date: String): List<IncomeRemote>
}