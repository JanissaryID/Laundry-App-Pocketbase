package com.aluma.laundry.data.machine.local

import com.aluma.laundry.data.machine.model.MachineLocal
import kotlinx.coroutines.flow.Flow

class MachineLocalRepository(private val dao: MachineDAO) {
    val machineLocal: Flow<List<MachineLocal>> = dao.getAllMachine()

    suspend fun addMachine(machineLocal: MachineLocal) = dao.insert(machineLocal)
    suspend fun deleteMachine(machineLocal: MachineLocal) = dao.delete(machineLocal)
    suspend fun deleteAllMachines() = dao.deleteAll()
    suspend fun updateMachine(machineLocal: MachineLocal): Int{
        return dao.update(machineLocal)
    }
    suspend fun getMachineById(id: String): MachineLocal? = dao.getMachineById(id)
}